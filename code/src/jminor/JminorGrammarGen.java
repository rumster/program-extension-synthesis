package jminor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import bgu.cs.util.treeGrammar.Grammar;
import bgu.cs.util.treeGrammar.Nonterminal;

/**
 * A grammar for the Jminor language.
 * 
 * @author romanm
 */
public class JminorGrammarGen {
	public static final Nonterminal ncond = new Nonterminal("NCond");

	/**
	 * a view of the grammar (not a component): A op B A - nonterminal B -
	 * nonterminal, null op <code>- ==/<=/< </code>
	 */
	public static final Nonterminal neq_arithm = new Nonterminal("NSimple");

	/**
	 * a view of the grammar (not a component): A op B A - nonterminal B -
	 * nonterminal, null
	 */
	public static final Nonterminal neq = new Nonterminal("NSimple");

	/**
	 * A nonterminal for a single disjunct.
	 */
	private static final Nonterminal nbasic = new Nonterminal("NBasic");
	private static final Nonterminal nstmt = new Nonterminal("NStmt");
	private static final Nonterminal naccpath = new Nonterminal("NAccPath");

	/**
	 * Constructs a Jminor grammar for the given variables and types.
	 * 
	 * TODO: missing allocation statements.
	 * 
	 * @param vars
	 *            A collection of variables.
	 * @param refTypes
	 *            A collection of object types.
	 */
	public static Grammar gen(Collection<Var> vars, Collection<RefType> refTypes) {
		Grammar result = new Grammar(nstmt);

		Map<Type, Nonterminal> typeToAPathNonterminal = new HashMap<>();
		for (RefType refType : refTypes) {
			Nonterminal nonterminal = new Nonterminal("N" + refType.getName());
			result.add(nonterminal);
			typeToAPathNonterminal.put(refType, nonterminal);
		}
		Nonterminal intNonterminal = new Nonterminal("NInt");
		typeToAPathNonterminal.put(IntType.v, intNonterminal);
		result.add(intNonterminal);

		result.add(nstmt);
		result.add(ncond);
		result.add(nbasic);
		result.add(naccpath);

		WhileStmt oJminor = new WhileStmt(ncond, nstmt);
		nstmt.add(oJminor);
		IfStmt opIf = new IfStmt(ncond, nstmt, nstmt);
		nstmt.add(opIf);
		SeqStmt opSeq = new SeqStmt(nstmt, nstmt);
		nstmt.add(opSeq);

		// TODO
		// AssignStmt opAssgn1 = new AssignStmt(naccpath, naccpath);
		// AssignStmt opAssgn2 = new AssignStmt(naccpath, NullExpr.v);
		// nstmt.add(opAssgn1);
		// nstmt.add(opAssgn2);

		OrExpr opOr = new OrExpr(ncond, ncond);
		ncond.add(opOr);
		ncond.add(nbasic);

		EqExpr opEq1 = new EqExpr(naccpath, naccpath);
		EqExpr opEq2 = new EqExpr(naccpath, NullExpr.v);
		nbasic.add(opEq1);
		nbasic.add(opEq2);
		nbasic.add(new NotExpr(opEq1));
		nbasic.add(new NotExpr(opEq2));
		EqExpr opEqInt = new EqExpr(intNonterminal, intNonterminal);
		nbasic.add(opEqInt);
		nbasic.add(new NotExpr(opEqInt));
		LtExpr opLt = new LtExpr(intNonterminal, intNonterminal);
		nbasic.add(opLt);
		nbasic.add(new NotExpr(opLt));

		AndExpr opAnd = new AndExpr(nbasic, nbasic);
		nbasic.add(opAnd);

		/////////
		neq_arithm.add(opEq1);
		neq_arithm.add(opEq2);
		neq_arithm.add(opEqInt);
		neq_arithm.add(opLt);
		/////////
		neq.add(opEq1);
		neq.add(opEq2);
		/////////

		for (Var var : vars) {
			if (var instanceof RefVar) {
				RefType type = ((RefVar) var).getType();
				Nonterminal nonterminal = typeToAPathNonterminal.get(type);
				nonterminal.add(new VarExpr(var));
			} else if (var instanceof PrimitiveVar) {
				intNonterminal.add(new VarExpr(var));
			} else {
				assert false : "encountered unexpected type of variables!";
			}
		}

		for (Map.Entry<Type, Nonterminal> entry : typeToAPathNonterminal.entrySet()) {
			Type type = entry.getKey();
			Nonterminal nonterminal = entry.getValue();
			if (type instanceof RefType) {
				naccpath.add(nonterminal);
				RefType refType = (RefType) type;
				for (Field field : refType.fields) {
					Nonterminal apathNonterm = typeToAPathNonterminal.get(field.dstType);
					apathNonterm.add(new DerefExpr(nonterminal, field));
				}
			}
		}

		return result;
	}
}
