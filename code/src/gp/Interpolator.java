package gp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.logic.*;
import de.uni_freiburg.informatik.ultimate.logic.Annotation;
import de.uni_freiburg.informatik.ultimate.logic.ApplicationTerm;
import de.uni_freiburg.informatik.ultimate.logic.Logics;
import de.uni_freiburg.informatik.ultimate.logic.SMTLIBException;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Script.LBool;
import de.uni_freiburg.informatik.ultimate.logic.Sort;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.smtinterpol.smtlib2.SMTInterpol;
import grammar.CachedLanguageIterator;
import heap.AndExpr;
import heap.BoolExpr;
import heap.EqExpr;
import heap.NotExpr;
import heap.OrExpr;

public class Interpolator {
	private static final String v = "var";
	private static final String fictv = "fictvar";
	private static final String c = "conj";
	private static final String d = "disj";

	// new sort for Reference type
	private static final String REF_SORT = "Ref";
	// built-in sorts
	//private static final String INT_SORT = "Int";
	private static final String BOOL_SORT = "Bool";

	private Script s;
	private int varCounter = 0;
	private Map<String, BoolExpr> variables;

	//TODO outputDir - fetch from global configurations
	public Interpolator(String outputDir) {
		variables = new HashMap<>();

		s = new SMTInterpol();
		s.setOption(":produce-proofs", true);
		s.setLogic(Logics.QF_UFLIA);
		s.declareSort(REF_SORT, 0);

		// TODO: define a custom logger (log4j is required) instead of the default one
		s.setOption(":diagnostic-output-channel", outputDir + File.separator + "SMTInterpol.log");
	}

	/**
	 * Generate an interpolant between given predicates opsl and opsr. The
	 * predicates are two dimensional arrays, representing DNF: each row represents
	 * a conjunctive clause between it's elements all the rows together represent a
	 * disjunctive clause
	 * 
	 * @param opsl
	 * @param opsr
	 * @param citer
	 *            - language iterator to get the actual predicates
	 * @return an interpolant betweeen opsl and opsr, "null" if it wasn't found
	 */
	public BoolExpr genInterpolant(List<List<Boolean>> opsl, List<List<Boolean>> opsr, CachedLanguageIterator citer) {
		BoolExpr result = null;
		Term tl = predicatesToTerm(opsl, citer);
		Term tr = predicatesToTerm(opsr, citer);
		Term resultTerm = genInterpolant(tl, tr);
		if (resultTerm != null) {
			result = termToPredicate(resultTerm);
		}
		return result;
	}

	/*
	 * Convert the given predicate to equivalent SMTInterpol Term
	 */
	private Term predicatesToTerm(Collection<List<Boolean>> ops, CachedLanguageIterator citer) {
		List<Term> disj = new ArrayList<>();
		for (List<Boolean> l : ops) {
			List<Term> conj = new ArrayList<>();
			for (int i = 0; i < l.size(); i++) {
				// i's predicate value is undefined
				if (l.get(i) == null) {
					continue;
				}

				// define a fictitious boolean variable for each predicate
				String var = fictv + i;
				if (!variables.containsKey(var)) {
					//TODO: unsafe casting to BoolExpr - refactor
					variables.put(var, (BoolExpr)citer.get(i));
					s.declareFun(var, new Sort[0], s.sort(BOOL_SORT));
				}
				Term t = s.term(var);
				if (!l.get(i)) {
					t = s.term("not", t);
				}
				t = s.annotate(t, new Annotation(":named", v + (varCounter++)));
				conj.add(t);
			}

			Term tconj;
			if (conj.size() == 1) {
				tconj = conj.get(0);
			} else {
				tconj = s.annotate(s.term("and", conj.toArray(new Term[conj.size()])),
						new Annotation(":named", c + (varCounter++)));
			}
			disj.add(tconj);
		}

		Term tdisj = null;
		if (disj.size() == 1) {
			tdisj = disj.get(0);
		} else if (disj.size() > 1) {
			tdisj = s.annotate(s.term("or", disj.toArray(new Term[disj.size()])),
					new Annotation(":named", d + (varCounter++)));
		}
		return tdisj;
	}

	/*
	 * Convert given SMTInterpol term to equivalent PWhile operator
	 * 
	 * @t: ApplicationTerm only
	 */
	private BoolExpr termToPredicate(Term t) {
		if (!(t instanceof ApplicationTerm)) {
			// we're not supposed to get anything else (e.g. ConstantTerm)
			throw new UnsupportedOperationException(t.toString());
		}

		BoolExpr result = null;
		ApplicationTerm thisTerm = (ApplicationTerm) t;
		String name = thisTerm.getFunction().getName();
		Term[] params = thisTerm.getParameters();

		List<BoolExpr> opParams = new ArrayList<>();
		for (Term param : params) {
			BoolExpr n = termToPredicate(param);
			if (n == null) {
				return null;
			} else {
				opParams.add(n);
			}
		}
		if (params.length == 0) {
			// fictitious variable - get actual predicate from the language iterator
			result = variables.get(name);
		} else {
			switch (name) {
			case "=":
				result = new EqExpr(opParams.get(0), opParams.get(1));
				break;
			case "not":
				result = new NotExpr(opParams.get(0));
				break;
			case "or":
				result = opParams.get(0);
				for (int i = 1; i < params.length; i++) {
					BoolExpr n1 = result;
					BoolExpr n2 = opParams.get(i);
					result = new OrExpr(n1, n2);
				}
				break;
			case "and":
				result = opParams.get(0);
				for (int i = 1; i < params.length; i++) {
					BoolExpr n1 = result;
					BoolExpr n2 = opParams.get(i);
					result = new AndExpr(n1, n2);
				}
				break;
			case "=>": // is returned sometimes by SMPInterpol
				result = new NotExpr(new AndExpr(opParams.get(0), new NotExpr(opParams.get(1))));
				break;
			case "distinct":
				/*
				 * the (pairwise) inequality function among the elements of any set of values of
				 * the same sort
				 */
				result = new NotExpr(new EqExpr(opParams.get(opParams.size() - 2), opParams.get(opParams.size() - 1)));
				for (int i = 0; i < opParams.size() - 1; i++) {
					for (int j = i + 1; j < opParams.size(); j++) {
						BoolExpr n1 = opParams.get(i);
						BoolExpr n2 = opParams.get(j);
						result = new AndExpr(result, new NotExpr(new EqExpr(n1, n2)));
					}
				}
				break;
			case "ite":
				// if-then-else operator
				// ite(A, B, C) = A&B or !A&C
				BoolExpr A = opParams.get(0);
				BoolExpr B = opParams.get(1);
				BoolExpr C = opParams.get(2);
				result = new OrExpr(new AndExpr(A, B), new AndExpr(new NotExpr(A), C));
				break;
			default:
				result = null;
				break;
			}
		}

		return result;
	}

	/*
	 * remarks for future modifications: 1) must use Script.push(1) - Script.pop(1)
	 * for consecutive interpolations using the same Script object 2) one may try to
	 * simplify the result term using Script.simplify (looks buggy)
	 */
	private Term genInterpolant(Term t1, Term t2) {
		Term result = null;
		try {
			s.push(1);
			s.assertTerm(t1);
			s.assertTerm(t2);
			if (s.checkSat() == LBool.UNSAT) {
				Annotation[] a1 = ((AnnotatedTerm) t1).getAnnotations();
				Annotation[] a2 = ((AnnotatedTerm) t2).getAnnotations();

				Term[] interpolants;
				interpolants = s.getInterpolants(
						new Term[] { s.term((String) a1[0].getValue()), s.term((String) a2[0].getValue()) });
				if (interpolants.length > 0) {
					// s.pop(1);
					// result = s.simplify(interpolants[0]);
					// return result;
					result = interpolants[0];
				}
			}
		} catch (SMTLIBException ex) {
			//Globals.LOGGER.warning("SMTIntepol failed: " + ex.getMessage());
			ex.printStackTrace(System.err);
		} finally {
			s.pop(1);
		}
		return result;
	}

	// /**
	// * Generate an interpolant between given predicates opsl and opsr.
	// * The predicates are two dimensional arrays, representing DNF:
	// * each row represents a conjunctive clause between it's elements
	// * all the rows together represent a disjunctive clause
	// * @param opsl
	// * @param opsr
	// * @return an interpolant betweeen opsl and opsr, "null" if it wasn't found
	// */
	// public Operator genInterpolant(List<List<Operator>> opsl,
	// List<List<Operator>> opsr){
	// Operator result = null;
	// Term tl = operatorsToTerm(opsl);
	// Term tr = operatorsToTerm(opsr);
	// if(tl != null && tr != null){
	// Term resultTerm = genInterpolant(tl, tr);
	// result = (Operator)genOperator(resultTerm);
	// }
	// return result;
	// }
	//
	// /*
	// * Convert the given predicate to equivalent SMTInterpol Term
	// */
	// private Term operatorsToTerm(Collection<List<Operator>> ops){
	// List<Term> disj = new ArrayList<>();
	// for(List<Operator> l : ops){
	// List<Term> conj = new ArrayList<>();
	// for(Operator op : l){
	// Term t = genTerm(op);
	// if(t == null){
	// return null;
	// }
	// conj.add(t);
	// }
	//
	// Term tconj;
	// if(conj.size() == 1){
	// tconj = conj.get(0);
	// }else{
	// tconj = s.annotate(
	// s.term("and", conj.toArray(new Term[conj.size()])),
	// new Annotation(":named", c + (varCounter++)));
	// }
	// disj.add(tconj);
	// }
	//
	// Term tdisj = null;
	// if(disj.size() == 1){
	// tdisj = disj.get(0);
	// }else if (disj.size() > 1){
	// tdisj = s.annotate(
	// s.term("or", disj.toArray(new Term[disj.size()])),
	// new Annotation(":named", d + (varCounter++)));
	// }
	// return tdisj;
	// }
	//
	// /*
	// * Convert given PWhile operator to equivalent SMTInterpol term
	// */
	// private Term genTerm(Node node){
	// Term result = null;
	// shemp.grammar.Sort sort = node.getSort();
	// String opname = opnames.get(sort);
	// if(opname != null){ //Operator
	// Operator op = (Operator)node;
	// List<Term> params = new ArrayList<>();
	// for(Node n : op.getArgs()){
	// Term t = genTerm(n);
	// if(t == null){
	// return null;
	// }
	// else{
	// params.add(t);
	// }
	// }
	// result = s.annotate(
	// s.term(opname, params.toArray(new Term[params.size()])),
	// new Annotation(":named", v + (varCounter++)));
	//
	// }else { //Variable
	// String type;
	// if(sort == PWhileGrammarGen.ADDRESS_SORT){
	// type = REF_SORT;
	// }else if(sort == PWhileGrammarGen.INT_SORT){
	// type = INT_SORT;
	// }else if(sort == OpDeref.DEREF_SORT){
	// type = REF_SORT;
	// TType t = ((OpDeref)node).dstType();
	// if(t instanceof TIntType){
	// type = INT_SORT;
	// }
	// } else{
	// // not (supported) Operator or Variable
	// return null;
	// }
	// if(!variables.containsKey(node.toString())){
	// variables.put(node.toString(), node);
	// s.declareFun(node.toString(), new Sort[0], s.sort(type));
	// }
	// result = s.term(node.toString());
	// }
	// return result;
	// }
	//
	// /*
	// * Convert given SMTInterpol term to equivalent PWhile operator
	// *
	// * @t: ConstantTerm (i.e. an integer) or ApplicationTerm
	// */
	// private Node genOperator(Term t){
	// Node result = null;
	// if(t instanceof ConstantTerm){
	// ConstantTerm thisTerm = (ConstantTerm)t;
	// if(thisTerm.getSort().isNumericSort()){
	// BigInteger val = (BigInteger)thisTerm.getValue();
	// result = new TInt(val.intValue());
	// }
	// else{
	// throw new UnsupportedOperationException(thisTerm.toString());
	// }
	// }
	// else{
	// ApplicationTerm thisTerm = (ApplicationTerm)t;
	// String name = thisTerm.getFunction().getName();
	// Term[] params = thisTerm.getParameters();
	//
	// List<Node> opParams = new ArrayList<>();
	// for(Term param : params){
	// Node n = genOperator(param);
	// if(n == null){
	// return null;
	// }
	// else{
	// opParams.add(n);
	// }
	// }
	// if(params.length == 0){
	// result = variables.get(name);
	// }
	// else{
	// switch(name){
	// case "=":
	// result = new OpEq(opParams.get(0), opParams.get(1));
	// break;
	// case "<":
	// result = new OpLt(opParams.get(0), opParams.get(1));
	// break;
	// case ">":
	// result = new OpLt(opParams.get(1), opParams.get(0));
	// break;
	// case "<=":
	// result = new OpLeq(opParams.get(0), opParams.get(1));
	// break;
	// case ">=":
	// result = new OpLeq(opParams.get(1), opParams.get(0));
	// break;
	// case "+":
	// result = new OpPlus(opParams.get(0), opParams.get(1));
	// break;
	// case "not":
	// result = new OpNot(opParams.get(0));
	// break;
	// case "-":
	// result = new OpMinus(opParams.get(0));
	// break;
	// case "or":
	// result = opParams.get(0);
	// for(int i = 1; i < params.length; i++){
	// Node n1 = result;
	// Node n2 = opParams.get(i);
	// result = new OpOr(n1, n2);
	// }
	// break;
	// case "and":
	// result = opParams.get(0);
	// for(int i = 1; i < params.length; i++){
	// Node n1 = result;
	// Node n2 = opParams.get(i);
	// result = new OpAnd(n1, n2);
	// }
	// break;
	// case "=>": //is returned sometimes by SMPInterpol
	// result = new OpNot(new OpAnd(opParams.get(0), new OpNot(opParams.get(1))));
	// break;
	// case "distinct":
	// /*the (pairwise) inequality function among the elements of any set of values
	// of the same sort*/
	// result = new OpNot(new OpEq(opParams.get(opParams.size()-2),
	// opParams.get(opParams.size()-1)));
	// for(int i = 0; i < opParams.size()-1; i++){
	// for(int j = i+1; j < opParams.size(); j++){
	// Node n1 = opParams.get(i);;
	// Node n2 = opParams.get(j);
	// result = new OpAnd(result, new OpNot(new OpEq(n1, n2)));
	// }
	// }
	// break;
	// case "ite":
	// //if-then-else operator
	// //ite(A, B, C) = A&B or !A&C
	// Node A = opParams.get(0);
	// Node B = opParams.get(1);
	// Node C = opParams.get(2);
	// result = new OpOr(new OpAnd(A, B), new OpAnd(new OpNot(A), C));
	// break;
	// default:
	// result = null;
	// break;
	// }
	// }
	// }
	// return result;
	// }
}
