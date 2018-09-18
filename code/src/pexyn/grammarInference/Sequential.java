package pexyn.grammarInference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.logging.Logger;

import bgu.cs.util.rel.HashRel2;
import jminor.BoolExpr;
import jminor.JmStore;
import jminor.Stmt;
import pexyn.Semantics.Cmd;
import pexyn.Semantics.Guard;
import pexyn.Semantics.Store;
import pexyn.guardInference.ConditionInferencer;

/**
 * @author User
 *
 */
public class Sequential extends Generalizer {
	protected final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	ArrayList<Nonterminal> nts = grammar.getNonterminals();
	SententialForm startProd = null;
	static int ntsCounter = 0;

	ArrayList<BiPredicate<InputParseState, Grammar>> externalTranformers = new ArrayList<>();
	ArrayList<Predicate<Grammar>> internalTranformers = new ArrayList<>();
	private ConditionInferencer<JmStore, Stmt, BoolExpr> separator;

	public Sequential() {
		super();
		ntsCounter = 0;
		// Adding order is important here - from most benefit to least.
		externalTranformers.add(Sequential::extAppendNonterminal);
		externalTranformers.add(Sequential::extAppendTerminal);

		internalTranformers.add(Sequential::intMergeLastSymbol);
		internalTranformers.add(Sequential::intFindLoops);
		internalTranformers.add(Sequential::intFindBlocks);
		internalTranformers.add(Sequential::intFindConds);
	}

	private class InputParseState {
		List<? extends Letter> cmds;
		int index = 0;

		public InputParseState(List<? extends Letter> cmds2) {
			assert (cmds2 != null);
			cmds = cmds2;
		}

		public Letter getCurr() {
			return cmds.get(index);
		}

		public List<? extends Letter> getScope() {
			return cmds.subList(index, cmds.size());
		}

		public boolean EOF() {
			return index == cmds.size();
		}

		@Override
		public String toString() {
			return getScope().toString();
		}
	}

	@Override
	public void append(List<? extends Letter> cmds) {
		if (startProd == null)
			newStartProd();
		InputParseState input = new InputParseState(cmds);
		while (!input.EOF()) {
			grammar.Sort();
			boolean transformed = false;
			for (BiPredicate<InputParseState, Grammar> transformer : externalTranformers) {
				if (transformer.test(input, grammar)) {
					transformed = true;
					break;
				}
			}
			assert (transformed);
			checkReps();
		}
	}

	private static boolean extAppendTerminal(InputParseState input, Grammar grammar) {
		grammar.getCurrStartProduct().add(new Terminal(input.getCurr()));
		input.index = input.index + 1;
		return true;
	}

	private static boolean extAppendNonterminal(InputParseState input, Grammar grammar) {
		int matchLen = -1;
		for (Nonterminal nt : grammar.getNonterminals()) {
			List<? extends Letter> scope = input.getScope();
			matchLen = nt.match(scope, true);
			if (matchLen > 0) {
				grammar.getCurrStartProduct().add(nt);
				input.index += matchLen;
				return true;
			} 
		}
		return false;
	}

	private void checkReps() {
		boolean changed;
		do {
			changed = false; //allows priority in the transformers order
			for (Predicate<Grammar> transformer : internalTranformers)
				if(transformer.test(grammar)) {
					changed = true;
					break;
				}
		} while (changed);
	}

	// to apply rules we must assume some properties to nonterminals, in here we
	// assume a recursive nt should only have 2 products
	private static boolean ntGeneratesSymbol(Symbol symb, Nonterminal nt) {
		if (!nt.getIsRecursive())
			return false;

		if (nt.getProductsGenerated().contains(symb) || symb == nt)
			return true;
		for (Symbol generated : nt.getProductsGenerated()) {
			if (generated == nt)
				continue;
			if (generated.getClass() == Nonterminal.class) {
				if (ntGeneratesSymbol(symb, (Nonterminal) generated)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static Nonterminal makeRecNt(Grammar grammar, Symbol symb1) {
		Nonterminal newnt = new Nonterminal("R" + ntsCounter++);
		newnt.recursive = true;
		ReplaceAppearances(grammar, newnt, symb1, false); // replace any appearance of the symbol with its rec representation
		SententialForm opt1 = new SententialForm();
		Nonterminal symbnt = null;
		if(symb1.getClass() == Nonterminal.class) {
			symbnt = (Nonterminal) symb1;
		}
		if(symbnt != null && symbnt.ifNt) {
			removeUnusedNt(grammar,symbnt);
			var body = symbnt.getProductions().get(0);
			assert(body.size() > 0);
			opt1.addAll(body);
		} else {
			opt1.add(symb1);
		}
		// ! @TODO Merge contexts with symb2 too if they're terminals
		newnt.SetRec(opt1);
		grammar.add(newnt);
		return newnt;
	}

	private static void removeUnusedNt(Grammar grammar, Nonterminal symbnt) {
		int count = 0;
		if(grammar.getCurrStartProduct().contains(symbnt)) count++;
		for(Nonterminal nt: grammar.getNonterminals())
			for(SententialForm sent: nt.getProductions())
				if(sent.contains(symbnt)) count++;
		if(count == 0) {
			grammar.getNonterminals().remove(symbnt);
		}
		
	}

	private static void removeUnusedNts(Nonterminal newnt, Grammar grammar) {
		SententialForm startProd = grammar.getCurrStartProduct();
		ArrayList<Nonterminal> nts = grammar.getNonterminals();
		boolean replacedNt = true;
		while (replacedNt) {
			replacedNt = false;
			// removing nts used once only
			final SententialForm sub = newnt.getProductions().get(0);
			HashSet<Nonterminal> rhsNts = new HashSet<>();
			for (int i = 0; i < sub.size(); ++i) {
				if (sub.get(i).getClass() == Nonterminal.class) {
					rhsNts.add((Nonterminal) sub.get(i));
				}
			}
			HashSet<Nonterminal> removeRhsNts = new HashSet<>();
			for (Nonterminal on : rhsNts) {
				if(on.getProductions().size() > 1)
					removeRhsNts.add(on);

				int count = 0;

				for (Symbol s : startProd) {
					if (s.equals(on))
						count++;
				}
				for (Nonterminal nt : nts) {
					for (SententialForm op : nt.getProductions()) {
						for (Symbol s : op) {
							if (s.equals(on))
								count++;
						}
					}
				}
				if (count > 1)
					removeRhsNts.add(on);

			}
			rhsNts.removeAll(removeRhsNts);
			// anything left in rhsNts can be swapped by its content.
			SententialForm newSub = new SententialForm();
			for (Symbol op : sub) {
				if (rhsNts.contains(op)) {					
					newSub.addAll(((Nonterminal) op).getProductions().get(0));
				} else {
					newSub.add(op);
				}
			}
			newnt.getProductions().clear();
			newnt.add(newSub);
			for (Nonterminal nt : rhsNts) {
				nts.remove(nt); // removing old unused nonterminals
			}
			// removing replacement nt's
			replacedNt = removeDuplicateNt(grammar);
		}
	}

	private static boolean removeDuplicateNt(Grammar grammar) {
		SententialForm startProd = grammar.getCurrStartProduct();
		ArrayList<Nonterminal> nts = grammar.getNonterminals();
		boolean replacedNt = false;
		for (Nonterminal nt1 : nts) {
			List<SententialForm> prods = nt1.getProductions();
			for (Nonterminal nt2 : nts) {
				if (nt1 == nt2)
					continue;
				if (prods.size() == 1 && prods.get(0).size() == 1) {
					if (prods.get(0).get(0).equals(nt2)) {
						nts.remove(nt2);
						// replacing nt2 prods with nt1's
						prods.clear();
						prods.addAll(nt2.getProductions());
						replacedNt = true;
						// and replacing nt2 appearances with nt1's
						for (int i = 0; i < startProd.size(); ++i) {
							if (startProd.get(i).equals(nt2))
								startProd.set(i, nt1);
						}
						for (Nonterminal nt : nts) {
							for (SententialForm prod : nt.getProductions()) {
								for (int i = 0; i < prod.size(); ++i) {
									if (prod.get(i).equals(nt2))
										prod.set(i, nt1);
								}
							}
						}
						return true;
					}
				}
			}
		}
		return replacedNt;
	}

	private static boolean intMergeLastSymbol(Grammar grammar) {
		SententialForm startProd = grammar.getCurrStartProduct();
		int size = startProd.size();
		if (size < 2)
			return false;
		if (startProd.get(size - 2).getClass() == Nonterminal.class) {
			Nonterminal nt = (Nonterminal) startProd.get(size - 2);
			Symbol symb = startProd.get(size - 1);
			if (ntGeneratesSymbol(symb, nt)) {
				startProd.remove(startProd.size() - 1);
				return true;
			}
		}
		return false;
	}


	/**
	 * Check for loops. if the current symbol repeats itself twice, put it in a
	 * recursive rule.
	 * 
	 * @param grammar
	 * 
	 * @return
	 */
	private static boolean intFindLoops(Grammar grammar) {
		SententialForm startProd = grammar.getCurrStartProduct();
		int size = startProd.size();
		if (size < 2)
			return false;
		Symbol symb1 = startProd.get(size - 2);
		Symbol symb2 = startProd.get(size - 1);
		if (symb1.equals(symb2)) {
			// Found a loop. Extract that into a new nt.
			makeRecNt(grammar, symb1);
			startProd.remove(--size);
			return true;
		} else if (symb1.getClass() == Nonterminal.class && symb2.getClass() == Nonterminal.class) {
			// complete this?
		} else if (symb1.getClass() == Nonterminal.class) {
			Nonterminal nt1 = (Nonterminal) symb1;
			SententialForm sent1 = nt1.expand();
			if (sent1.size() > 0) {
				if (sent1.get(sent1.size() - 1).equals(symb2)) {
					// ! @note only looks at prod #0 atm
					Nonterminal newnt = makeRecNt(grammar, symb2);
					Nonterminal nt = nt1.getIsRecursive() ? (Nonterminal) nt1.getProductions().get(1).get(0) : nt1;
					SententialForm sent = nt.getProductions().get(0);
					sent.set(sent.size() - 1, newnt);
					startProd.remove(--size);
					return true;
				}
			}
		} else if (symb2.getClass() == Nonterminal.class) {
			Nonterminal nt2 = (Nonterminal) symb2;
			SententialForm sent2 = nt2.expand();
			if (sent2.size() > 0) {
				if (sent2.get(0).equals(symb1)) {
					// ! @note only looks at prod #0 atm
					Nonterminal newnt = makeRecNt(grammar, symb1);
					Nonterminal nt = nt2.getIsRecursive() ? (Nonterminal) nt2.getProductions().get(1).get(0) : nt2;
					SententialForm sent = nt.getProductions().get(0);
					sent.set(0, newnt);
					startProd.remove(--size - 1);
					return true;
				}
			}
		}
		return false;
	}
	
	private static boolean intFindBlocks(Grammar grammar) {
		SententialForm startProd = grammar.getCurrStartProduct();
		int size = startProd.size();
		if (size < 2)
			return false;
		ArrayList<Symbol> sub = new ArrayList<>(startProd.subList(size - 2, size));
		Nonterminal newnt = new Nonterminal("B" + ntsCounter);
		newnt.selective = true;
		newnt.add(new SententialForm(sub));

		boolean ret = false;
		ret |= findBlockInStart(newnt, sub, grammar);
		ret |= findBlockInNts(newnt, sub, grammar);
		if (ret) {
			size = startProd.size();
			startProd.remove(--size);
			startProd.remove(--size);
			startProd.add(newnt);
			grammar.getNonterminals().add(newnt);
			ntsCounter++;
			removeUnusedNts(newnt, grammar);
		}
		return ret;
	}


	/** internal transformer Replacing 3 spot blocks with an if or if/else statement
	 * @param grammar
	 * @return
	 */
	private static boolean intFindConds(Grammar grammar) {
		SententialForm startProd = grammar.getCurrStartProduct();
		int size = startProd.size();
		if (size < 4)
			return false;
		List<Symbol> sub = startProd.subList(size - 3, size);

		if(intFindCondsInStart(sub, grammar)) return true;
		if(intFindCondsInNts(sub, grammar)) return true;
		return false;
	}

	//sub is top 3 symbols. gonna look for if cases.
	private static boolean intFindCondsInStart(List<Symbol> sub, Grammar grammar) {
		SententialForm startProd = grammar.getCurrStartProduct();
		int size = startProd.size();
		if (size < 4)
			return false;
		
		Nonterminal midSubNt = null;
		if(sub.get(1).getClass() == Nonterminal.class) 
			midSubNt = (Nonterminal) sub.get(1);

		Nonterminal newnt = null;
		//first look: case where sub contains block A inside the "if":
		for (int i = 0; i < size - 4; ++i) {
			if (startProd.get(i).equals(sub.get(0)) && startProd.get(i+1).equals(sub.get(2))){
				if(midSubNt != null && (midSubNt.ifNt || midSubNt.getIsRecursive())) {
					newnt = midSubNt;
				} else {
					newnt = new Nonterminal("ISA" + ntsCounter++);
					newnt.ifNt = newnt.selective = true;
					newnt.add(new SententialForm(sub.subList(1, 2)));
					newnt.add(new SententialForm());
					startProd.set(size - 2, newnt);
					grammar.add(newnt);
				}
				startProd.add(i+1, newnt);
				return true;
			}
		}

		//second look: case where sub doesnt contain A:
		for (int i = 0; i < size - 4; ++i) {
			if (startProd.get(i).equals(sub.get(1)) && startProd.get(i+2).equals(sub.get(2))){
				
				if(startProd.get(i+1).getClass() == Nonterminal.class) {
					midSubNt = (Nonterminal) startProd.get(i+1);
				}
				if(midSubNt != null && (midSubNt.ifNt || midSubNt.getIsRecursive())) {
					newnt = midSubNt;
				} else {
					newnt = new Nonterminal("ISB" + ntsCounter++);
					newnt.ifNt = newnt.selective = true;
					newnt.add(new SententialForm(startProd.subList(i+1, i+2)));
					newnt.add(new SententialForm());
					startProd.set(i+1, newnt);
					grammar.add(newnt);
				}
				startProd.add(size - 1, newnt);
				return true;
			}
		}
		//last look: if/else case:
		for (int i = 0; i < size - 5; ++i) {
			if (startProd.get(i).equals(sub.get(0)) && startProd.get(i+2).equals(sub.get(2))){
				if(startProd.get(i+1).equals(sub.get(1))) continue; //nothing to do here
				newnt = new Nonterminal("ISE" + ntsCounter++);
				newnt.ifElseNt = newnt.selective = true;
				newnt.add(new SententialForm(startProd.subList(i+1, i+2)));
				newnt.add(new SententialForm(sub.subList(1, 2)));
				startProd.set(size - 2, newnt);
				startProd.set(i+1, newnt);
				grammar.add(newnt);
				return true;
			}
		}


		return false;
	}

	private static boolean intFindCondsInNts(List<Symbol> sub, Grammar grammar) {
		SententialForm startProd = grammar.getCurrStartProduct();
		int size = startProd.size();
		if (size < 4)
			return false;
		Nonterminal newnt = null;
		Nonterminal midSubNt = null;
		grammar.Sort();
		//first look: case where sub contains block A inside the "if":
		for (int i = 0; i < grammar.getNonterminals().size(); ++i) {
			Nonterminal nt = grammar.getNonterminals().get(i);
			for (SententialForm prod : nt.getProductions()) {
				for (int j = 0; j < prod.size() - 1; ++j) {
					if (prod.get(j).equals(sub.get(0)) && prod.get(j+1).equals(sub.get(2))){
						if(sub.get(1).getClass() == Nonterminal.class) 
							midSubNt = (Nonterminal) sub.get(1);
						if(midSubNt != null && (midSubNt.ifNt || midSubNt.getIsRecursive())) {
							newnt = midSubNt;
						} else {
							newnt = new Nonterminal("INA" + ntsCounter++);
							newnt.ifNt = true;
							newnt.add(new SententialForm(sub.subList(1, 2)));
							newnt.add(new SententialForm());
							startProd.set(size - 2, newnt);
							grammar.add(newnt);
						}
						prod.add(j+1, newnt);
						return true;
					}
				}
			}
		}
		//second look: case where sub doesnt contain A:
		for (int i = 0; i < grammar.getNonterminals().size(); ++i) {
			Nonterminal nt = grammar.getNonterminals().get(i);
			for (SententialForm prod : nt.getProductions()) {
				for (int j = 0; j < prod.size() - 2; ++j) {
					if (prod.get(j).equals(sub.get(1)) && prod.get(j+2).equals(sub.get(2))){
						
						if(prod.get(j+1).getClass() == Nonterminal.class) {
							midSubNt = (Nonterminal) prod.get(j+1);
						}
						if(midSubNt != null && (midSubNt.ifNt || midSubNt.getIsRecursive())) {
							newnt = midSubNt;
						} else {
							newnt = new Nonterminal("INB" + ntsCounter++);
							newnt.ifNt = true;
							newnt.add(new SententialForm(prod.subList(j+1, j+2)));
							newnt.add(new SententialForm());
							prod.set(j+1, newnt);
						}
						startProd.add(size - 1, newnt);
						grammar.add(newnt);
						return true;
					}
				}
			}
		}
		//last look: if/else case:
		for (int i = 0; i < grammar.getNonterminals().size(); ++i) {
			Nonterminal nt = grammar.getNonterminals().get(i);
			for (SententialForm prod : nt.getProductions()) {
				for (int j = 0; j < prod.size() - 2; ++j) {
					if (prod.get(j).equals(sub.get(0)) && prod.get(j+2).equals(sub.get(2))){
						
						if(prod.get(j+1).getClass() == Nonterminal.class) {
							midSubNt = (Nonterminal) prod.get(j+1);
						}
						if(midSubNt != null && (midSubNt.ifNt || midSubNt.getIsRecursive())) {
							newnt = midSubNt;
						} else {
							Symbol s = prod.get(j+1);
							Nonterminal ns = null;
							if(s.getClass() == Nonterminal.class) {
								ns = (Nonterminal) s;
							}
							if(ns != null && ns.ifElseNt) {
								newnt = ns;
							} else {
								newnt = new Nonterminal("INE" + ntsCounter++);
								newnt.ifElseNt = newnt.selective = true;
								newnt.add(new SententialForm(prod.subList(j+1, j+2)));
								prod.set(j+1, newnt);
								grammar.add(newnt);
							}
						}
						newnt.add(new SententialForm(sub.subList(1,2)));
						startProd.set(size - 2, newnt); //im looking at the latest 3 symbols, found a match , might as well replace them.
						//sub.clear();
						//sub.add(nt);
						grammar.compressRecursion();
						return true;
					}
				}
			}
		}

		return false;
	}
	
	/** Replaces any appearance of s2 with s1
	 * @param grammar
	 * @param nt1
	 * @param nt2
	 */
	private static void ReplaceAppearances(Grammar grammar, Symbol s1, Symbol s2, boolean removeNt) {
		if(s1 == s2) return;
		for(Nonterminal nt: grammar.getNonterminals()) {
			for(SententialForm sent: nt.getProductions()) {
				sent.replaceAll((Symbol s) -> (s==s2 ? s1 : s));
			}
		}
		for(SententialForm sent: grammar.getStartProduct()) {
			sent.replaceAll((Symbol s) -> (s==s2 ? s1 : s));
		}
		if(s1.equals(s2)) grammar.getNonterminals().remove(s2);
	}


	private static boolean finalHandleNtMismatch(Grammar grammar, List<Symbol> list, List<Symbol> list2) {
		boolean eqLen = list2.size() == list.size();
		Nonterminal nt =  (Nonterminal) list.get(0);
		if(nt.getIsSelective()) { // if its selective - add the prod2 to it
			if(eqLen) {
				nt.add(list2.subList(0, 1));
				list2.set(0, nt);
			} else { //gotta check for epsilon
				nt.add(new SententialForm());
				if(list2.size() == 0) {
					list2.add(nt);
				} else {
					list2.add(0, nt);
				}
			}
			return true;
		} else if (nt.getIsRecursive() && !eqLen) { //if its recursive - insert it in the mismatch place.
			if(list2.size() == 0) {
				list2.add(nt);
				grammar.compressRecursion(nt);
			} else {
				for(var sent: nt.RecBody()) {
					if(nt == list2.get(0) && nt == list2.get(list2.size()-1)) {
						if(finalListsMergeWrapper(grammar, sent, list2.subList(1, list2.size()-1))) {
							nt.SetRec(sent);
							list2.clear();
							list2.add(nt);
							grammar.compressRecursion(nt);
							return true;
						}
					}
					if(finalListsMergeWrapper(grammar, sent, list2)) {
						list2.clear();
						list2.add(nt);
						grammar.compressRecursion(nt);
						return true;
					}
				}
				return false;
			}
			return true;
		} else if (nt.getIsRecursive() && eqLen) { //if its recursive - check for unidentified body.
			for(var sent: nt.RecBody()) {
				if(sent.equals(list2.subList(0, 1))) {
					list2.set(0, nt);
					grammar.compressRecursion(nt);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Gonna check if after adding a new terminal to start, is there a repetition of
	 * symbols in the nts.
	 * 
	 * @param newnt
	 * @param sub
	 * @param grammar
	 * 
	 * @return
	 */
	private static boolean findBlockInNts(Nonterminal newnt, ArrayList<Symbol> sub, Grammar grammar) {
		SententialForm startProd = grammar.getCurrStartProduct();
		int size = startProd.size();
		if (size < 4)
			return false;

		boolean res = false;
		for (int i = 0; i < grammar.getNonterminals().size(); ++i) {
			Nonterminal nt = grammar.getNonterminals().get(i);
			if (nt == newnt)
				continue;
			for (SententialForm prod : nt.getProductions()) {
				for (int j = 0; j < prod.size() - 1; ++j) {
					if (prod.subList(j, j + 2).equals(sub)) {
						prod.remove(j);
						prod.remove(j);
						prod.add(j, newnt);
						res = true;
					}
				}
			}
		}
		return res;
	}

	private static boolean findBlockInStart(Nonterminal newnt, ArrayList<Symbol> sub, Grammar grammar) {
		SententialForm startProd = grammar.getCurrStartProduct();
		int size = startProd.size();
		if (size < 4)
			return false;

		int indx = -1;
		for (int i = 0; i < size - 3; ++i) {
			if (startProd.subList(i, i + 2).equals(sub)) {
				indx = i; // we can assume this is the only time since otherwise
				// we would have done it before.
				break;
			}
		}
		if (indx != -1) {
			startProd.remove(indx);
			startProd.remove(indx);
			startProd.add(indx, newnt);
			return true;
		}
		return false;
	}

	@Override
	public void endWord() {
		List<SententialForm> prods = grammar.getStart().getProductions();
		List<SententialForm> sub = prods.subList(0, prods.size() - 1);
		sub.remove(startProd); //a sublist isnt a copy. when removing startProd from sub we remove duplicates.
		wordTransformersCheck();
		startProd = null;
	}
	
	private void wordTransformersCheck() {
		boolean ret;
		var startprods = grammar.getStartProduct();
		do{
			if(startprods.size()<2) break;
			var oldProd = startprods.get(startprods.size()-2);
			var newProd = startprods.get(startprods.size()-1);
			if(oldProd.equals(newProd)) {
				startprods.remove(oldProd);
				ret = true; continue;
			}
			ret = wordsTransWrapper(grammar, newProd, oldProd);
		} while(ret);
		
	}
	
	//code dupe
	private static boolean wordsTransWrapper(Grammar grammar, 
			List<Symbol> newProd, List<Symbol>  oldProd) {
		int start =0, end =0, minsize = Math.min(oldProd.size(), newProd.size());
		while(start < minsize && newProd.get(start).equals(oldProd.get(start))) {
			start++;
		}
		while(end < minsize && newProd.get(newProd.size() - 1 - end).equals(oldProd.get(oldProd.size() - 1 - end))) {
			end++;
		}
		if(start == minsize || end == minsize) return true;
		if(start + end > minsize) { //this is the case of an "if" inside the already inferred loop.
			start--;
			end--;
			assert(start+end < minsize);
		}
		return wordTransExec(grammar, newProd.subList(start, newProd.size() - end), oldProd.subList(start, oldProd.size() - end));
	}

	private static boolean wordTransExec(Grammar grammar, List<Symbol> list, List<Symbol> list2) {
		if(list.isEmpty() && list2.isEmpty()) return false;
		CommonSentDesc res = SententialForm.getLongestCommonSubstring(list, list2);

		if(list.containsAll(list2) && list2.containsAll(list)){ //ifelse variation here
			return finalMakeIfElseLoop(grammar, list, list2);
		}
		if(list.size() <= 2 || list2.size() <= 2 || res.length == 0) { // call on transformers like if/else loop finders
			if(list.size() == 1 && list.get(0).getClass() == Nonterminal.class) {
				if(finalCheckIfElseLoop(grammar, list, list2)) return true;
				if(finalCheckGeneration(grammar, list,list2)) return true;
				if(finalHandleNtMismatch(grammar, list,list2)) return true;
			}
			if(list2.size() == 1 && list2.get(0).getClass() == Nonterminal.class) {
				if(finalCheckIfElseLoop(grammar, list2, list)) return true;		
				if(finalCheckGeneration(grammar, list2,list)) return true;	
				if(finalHandleNtMismatch(grammar, list2,list)) return true;	
			}
			if(list.size() == 1 && list2.size() <= 1) {
				if(handleSymbMismatch(grammar, list,list2)) return true;
			}
			if(list.size() == 0 && list2.size() == 1) {
				if(handleSymbMismatch(grammar, list2,list)) return true;
			}
			if(list.size() == 2 && list2.size() == 2) {
				//check for if/else loop
				if(list.get(0).equals(list2.get(1)) && list.get(1).equals(list2.get(0))) {
					finalCreateIfElseLoop(grammar, list,list2);
					return true;
				}
			}
			if(list.size() == 0 || list2.size() == 0) {
				Nonterminal newnt = new Nonterminal("E" + ntsCounter++);
				if(list.size() > 0) {
					newnt.SetIf(new SententialForm(list));
					list.clear();
				} else if(list2.size() > 0){
					newnt.SetIf(new SententialForm(list2));
					list2.clear();
				} else {
					return false; //base case: both empty, nothing to merge.
				}
				list.add(newnt);
				list2.add(newnt);
				grammar.add(newnt);
				return true;
				
			}

			if(res.length == 0) return false;
		}
		return false;
	}
	
	//returns whether convergence successful or not.
	public boolean endInput() {
		boolean ret;
		var startprods = grammar.getStartProduct();
		do{
			if(startprods.size()<2) break;
			List<SententialForm> sub = startprods.subList(0, startprods.size() - 1);
			sub.remove(startprods.get(startprods.size()-1)); //a sublist isnt a copy. when removing startProd from sub we remove duplicates.
			ret = finalMergeInput(grammar);
		} while(ret);
		ReshapeRec();
		return startprods.size() == 1;
	}
	private void ReshapeRec() {
		boolean again;
		do {
			again = false;
			var start = grammar.getCurrStartProduct();
			int i=0;
			for(; i< start.size() - 1; i++) {
				Symbol sym = start.get(i);
				if(sym instanceof Nonterminal) {
					Nonterminal nt= (Nonterminal) sym;
					if(nt.getIsRecursive()) {
						Symbol next = start.get(i+1);
						var intSym = nt.getProductions().get(0).get(0);
						if(intSym.equals(next)) {
							again = true;
							break;
						}
						if(intSym instanceof Nonterminal) {
							Nonterminal intNt= (Nonterminal) intSym;
							var intBody = intNt.getProductions().get(0);
							if(intBody.get(0).equals(next)) {
								intBody.remove(0);
								intBody.add(next);
								again = true;
								break;
							}
						}
					}
				}
			}
			if(again) {
				var temp = start.get(i);
				start.set(i, start.get(i+1));
				start.set(i+1, temp);
			}
		}while(again);
		
	}

	private static boolean finalMergeInput(Grammar grammar) {
		var startprods = grammar.getStartProduct();
		if (startprods.size() < 2) return false;
		var oldProd = startprods.get(startprods.size()-2);
		var newProd = startprods.get(startprods.size()-1);
		if(oldProd.equals(newProd)) return false;
		return finalListsMergeWrapper(grammar, newProd, oldProd);
	}
	
	private static boolean finalListsMergeWrapper(Grammar grammar, 
			List<Symbol> newProd, List<Symbol>  oldProd) {
		int start =0, end =0, minsize = Math.min(oldProd.size(), newProd.size());
		while(start < minsize && newProd.get(start).equals(oldProd.get(start))) {
			start++;
		}
		while(end < minsize && newProd.get(newProd.size() - 1 - end).equals(oldProd.get(oldProd.size() - 1 - end))) {
			end++;
		}
		if(start == minsize || end == minsize) return true;
		if(start + end > minsize) { //this is the case of an "if" inside the already inferred loop.
			start--;
			end--;
			assert(start+end < minsize);
		}
		return finalMergeProds(grammar, newProd.subList(start, newProd.size() - end), oldProd.subList(start, oldProd.size() - end));
	}

	private static boolean finalMergeProds(Grammar grammar, List<Symbol> list, List<Symbol> list2) {
		if(list.isEmpty() && list2.isEmpty()) return false;
		CommonSentDesc res = SententialForm.getLongestCommonSubstring(list, list2);

		if(wordTransExec(grammar,list,list2)) return true;
		if(res.length == 0) {
			return false;
		}
		boolean ret = false;
		ret |= finalMergeProds(grammar, list.subList(res.Myindx+1, list.size()), list2.subList(res.Oindx+1, list2.size()));
		ret |= finalMergeProds(grammar, list.subList(0, res.Myindx - res.length + 1), list2.subList(0, res.Oindx - res.length + 1));
		return ret;
	}


	//list.size() ==1, list2.size()<=1
	private static boolean handleSymbMismatch(Grammar grammar, List<Symbol> list, List<Symbol> list2) {
		boolean eqLen = list.size() == list2.size();
		Nonterminal newnt = new Nonterminal("C" + ntsCounter++);
		var sent1 = new SententialForm(list);
		if(eqLen) {
			var sent2 = new SententialForm(list2);
			newnt.SetIfElse(sent1, sent2);
			list2.set(0, newnt);
		} else { //means prod2 is longer, gotta add into prod1
			newnt.SetIf(sent1);
			list2.add(newnt);
		}
		list.set(0, newnt);

		grammar.getNonterminals().add(newnt);
		return true;
	}

	//list.size() is 1
	private static boolean finalCheckGeneration(Grammar grammar, List<Symbol> list, List<Symbol> list2) {
			var nt = (Nonterminal)list.get(0) ;
			if(nt.match(list2, true) == list2.size()){
				list2.clear();
				list2.add(nt);		
				return true;
			}
		return false;
	}

	//we already know list and list2 contain the same symbols. 
	//Gonna check for existence of recursive nonterminal and add to it.
	//otherwise put all of it in a new nonterminal.
	private static boolean finalMakeIfElseLoop(Grammar grammar, List<Symbol> list, List<Symbol> list2) {
		//checking for nt, gonna assume only first or last can be nonterminals.:
		Nonterminal nt = null;
		if(list.get(0).getClass() == Nonterminal.class) {
			nt = (Nonterminal) list.get(0);
		} else if (list.get(list.size()-1).getClass() == Nonterminal.class) {
			nt = (Nonterminal) list.get(list.size()-1);
		} else {
			assert(false);
		}
		assert(nt.getIsRecursive());
		list2.remove(nt);
		for(var sent: nt.RecBody()) {
			if(finalListsMergeWrapper(grammar, sent, list2)) {
				if(nt.getProductions().get(0).get(0) != nt.getProductions().get(0).get(0)) {
					nt.SetRec(sent);
				}
				list2.clear();
				list.clear();
				list2.add(nt);
				list.add(nt);
				grammar.compressRecursion(nt);
				return true;
			}
		}
		
		return false;
	}
	
	//list.size() is 1
	private static boolean finalCheckIfElseLoop(Grammar grammar, List<Symbol> list, List<Symbol> list2) {
			var nt = (Nonterminal)list.get(0) ;
			if(nt.ifNt) {
				SententialForm prod = new SententialForm(nt.getProductions().get(0));
				if(prod.containsAll(list2) && list2.containsAll(prod)) { //if list1 is (a,b|eps) and list2 is (b,a), this creates (a|b)*
					Terminal preTerm = null;
					boolean pullOut = false;
					for(Symbol term : prod) {
						//gonna check if term is a terminal appearing in some block  in prod, this implies symb being in all of them.
						if(term.getClass() != Terminal.class) continue;
						preTerm = (Terminal) term;
						for(Symbol symb : prod) {
							if(symb.getClass() != Nonterminal.class) continue;
							Nonterminal prodNt = (Nonterminal) symb;
							if(!prodNt.getIsSelective()) continue;
							for(SententialForm sent : prodNt.getProductions()) {
								if(sent.size() > 0 && sent.get(0).equals(term)) {
									pullOut = true;
									sent.remove(0);
								}
							}
						}
						if(pullOut) break;	
					}
					if(pullOut) list2.remove(preTerm);
					finalCreateIfElseLoop(grammar, list, list2);
					if(pullOut) {
						Nonterminal n = (Nonterminal) list.get(0);
						n.SetRec(Arrays.asList(preTerm, n.getProductions().get(0).get(0)));
					}
					grammar.getNonterminals().remove(nt);
					return true;
				}
			}
		
		return false;
	}

	private static void finalCreateIfElseLoop(Grammar grammar, List<Symbol> list, List<Symbol> list2) {
		//create ifelse nt in a loop - everything in prod
		Nonterminal newnt = new Nonterminal("F" + ntsCounter++);
		newnt.ifElseNt = newnt.selective = true;
		for(int i=0; i< list2.size(); ++i) {
			newnt.add(list2.subList(i, i+1));
		}
		grammar.add(newnt);
		Nonterminal newnt2 = new Nonterminal("FR" + ntsCounter++);
		newnt2.SetRec(new SententialForm(Arrays.asList(newnt)));
		list.clear();
		list2.clear();
		list.add(newnt2);
		list2.add(newnt2);
		grammar.add(newnt2);
	}

	private void newStartProd() {
		grammar.getStartProduct().add(new SententialForm());
		startProd = grammar.getCurrStartProduct();
	}

	public boolean assignGuards() {
		assert(separator != null);
		Map<Nonterminal, Map<Cmd, ? extends Guard>> res = new HashMap<>();
		Set<SententialForm> set = new HashSet<>();
		set.add(grammar.getCurrStartProduct());
		for(Nonterminal nt: grammar.getNonterminals()) set.addAll(nt.getProductions());
		for(Nonterminal nt: grammar.getNonterminals()) {
			if(!(nt.recursive || nt.ifNt || nt.ifElseNt)) continue;
			var updateToValue = new HashRel2<Cmd, Store>();
			Map<Stmt, Set<Store>> myMap = nt.FirstStmts();
			Map<Stmt, Set<Store>> oMap = new HashMap<Stmt, Set<Store>>();
			for(SententialForm sent : set) {
				for(int i=0; i<sent.size(); ++i) {
					if(sent.get(i).equals(nt)) {
						//look at i+1 for inference.
						if(i<sent.size()-1) {
							var next = sent.get(i+1);
							if (!nt.ifElseNt) {
								oMap.putAll(next.FirstStmts());
								if(next instanceof Nonterminal) {
									var nextnt = ((Nonterminal)next);
									if((nextnt.getIsRecursive() || nextnt.ifNt) && i< sent.size()-2) {
										oMap.putAll(sent.get(i+2).FirstStmts());
									}
								}
							}
						}
					}
				}
			}
			Stmt myStmt, oStmt = null;
			if(nt.ifElseNt) {
				for(Entry<Stmt, Set<Store>> pair : myMap.entrySet()) {
					for(var store : pair.getValue()) {
						updateToValue.add(pair.getKey(), store); //currently add all actions into the map for some reason, not by node state.
					}
					pair.getValue().toString();
				}
			} else {
				myStmt = (Stmt)myMap.keySet().iterator().next();
				for(Entry<Stmt, Set<Store>> pair : myMap.entrySet()) {
					for(var store : pair.getValue()) {
						updateToValue.add(myStmt, store); //currently add all actions into the map for some reason, not by node state.
					}
				}
				oStmt = (Stmt)oMap.keySet().iterator().next();
				for(Entry<Stmt, Set<Store>> pair : oMap.entrySet()) {
					for(var store : pair.getValue()) {
						updateToValue.add(oStmt, store); //currently add all actions into the map for some reason, not by node state.
					}
				}
			}
			if(updateToValue.isEmpty()) {
				logger.info("Condition inference failed for " + nt.getName() +
						": No runtime code coverage.");
				continue;
			}
			var optUpdateToGuard = separator.infer(updateToValue);
			if (!optUpdateToGuard.isPresent()) {
				logger.info("Condition inference failed for " + nt.getName() +
						": No distinct deterministic guard found.");
				continue;
			}
			Map<Cmd, ? extends Guard> updateToGuard = optUpdateToGuard.get();
			var ntGuards = nt.getGuards();
			ntGuards.clear();
			for(Stmt key : myMap.keySet()) {
				for(int i=0; i<nt.getProductions().size(); i++) {
					SententialForm prod = nt.getProductions().get(i);
					assert(prod.size()>0);
					var prodStmts = prod.get(0).FirstStmts();
					if (prodStmts.keySet().contains(key)) {
						var guard = updateToGuard.get(key);
						if(guard!= null) ntGuards.add(i, guard);
						break;
					}
				}
			}
			logger.info("Found guards for Nonterminal " + nt.toString() + ":" + nt.getGuards().toString());
			res.put(nt, updateToGuard);
			logger.fine(updateToGuard.toString());
		}
		return true;
	}
	
	public Grammar addExampleStates(ArrayList<StmtLetter> actionTrace) {
		InputParseState input = new InputParseState(actionTrace);
		grammar.getStart().match(input.getScope(), true);
		return grammar;
	}

	public void setSeperator(ConditionInferencer<JmStore, Stmt, BoolExpr> separator) {
		this.separator = separator;
	}

	public void clearStates() {
		Set<SententialForm> set = new HashSet<>();
		set.add(grammar.getCurrStartProduct());
		for(Nonterminal nt: grammar.getNonterminals()) set.addAll(nt.getProductions());
		for(SententialForm sent : set) {
			for(int i=0; i<sent.size(); ++i) {
				sent.get(i).states.clear();
			}
		}
	}


}
