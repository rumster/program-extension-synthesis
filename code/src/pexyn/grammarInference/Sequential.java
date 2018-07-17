package pexyn.grammarInference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * @author User
 *
 */
public class Sequential extends Generalizer {
	ArrayList<Nonterminal> nts = grammar.getNonterminals();
	SententialForm startProd = null;
	static int ntsCounter = 0;

	ArrayList<BiPredicate<InputParseState, Grammar>> externalTranformers = new ArrayList<>();
	ArrayList<Predicate<Grammar>> internalTranformers = new ArrayList<>();

	public Sequential() {
		super();
		// Adding order is important here - from most benefit to least.
		externalTranformers.add(Sequential::appendNonterminal);
		externalTranformers.add(Sequential::appendTerminal);

		internalTranformers.add(Sequential::mergeLastSymbol);
		internalTranformers.add(Sequential::findLoops);
		internalTranformers.add(Sequential::findBlocks);
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
				;
			}
			assert (transformed);
			checkReps();
		}
	}

	private static boolean appendTerminal(InputParseState input, Grammar grammar) {
		grammar.getCurrStartProduct().add(new Terminal(input.getCurr()));
		input.index = input.index + 1;
		return true;
	}

	private static boolean appendNonterminal(InputParseState input, Grammar grammar) {
		int matchLen = -1;
		for (Nonterminal nt : grammar.getNonterminals()) {
			List<? extends Letter> scope = input.getScope();
			matchLen = nt.match(scope);
			if (matchLen >= 0) {
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
			changed = false;
			for (Predicate<Grammar> transformer : internalTranformers)
				changed |= transformer.test(grammar);
		} while (changed);
	}

	private static boolean mergeLastSymbol(Grammar grammar) {
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
		/*
		 * for (SententialForm sent : nt.getProductions()) { Symbol lastSymbol =
		 * sent.get(sent.size() - 1); if (lastSymbol.equals(symb)) return true; if
		 * (lastSymbol instanceof Nonterminal) { Nonterminal lastNt = (Nonterminal)
		 * lastSymbol; if (lastNt == nt) { // not going to slip into stackoverflow here
		 * continue; } if (ntGeneratesSymbol(symb, lastNt)) return true;
		 * 
		 * } }
		 */
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
	private static boolean findLoops(Grammar grammar) {
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

	private static Nonterminal makeRecNt(Grammar grammar, Symbol symb1) {
		Nonterminal newnt = new Nonterminal("R" + ntsCounter++);
		ReplaceAppearances(grammar, newnt, symb1, false); // replace any appearance of the symbol with its rec representation
		SententialForm opt1 = new SententialForm();
		opt1.add(symb1);
		// ! @TODO Merge contexts with symb2 too if they're terminals
		SententialForm opt2 = new SententialForm(opt1);
		opt2.add(newnt);
		newnt.add(opt1);
		newnt.add(opt2);
		newnt.add(new SententialForm()); // and an empty option
		grammar.getNonterminals().add(newnt);
		return newnt;
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
					assert (((Nonterminal) op).getProductions().size() == 1);
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

	private static boolean findBlocks(Grammar grammar) {
		SententialForm startProd = grammar.getCurrStartProduct();
		int size = startProd.size();
		if (size < 2)
			return false;
		ArrayList<Symbol> sub = new ArrayList<>(startProd.subList(size - 2, size));
		Nonterminal newnt = new Nonterminal("R" + ntsCounter);
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

	// TODO optimize by only calling this with new nonterminals
	private static boolean findCondBlocks(Grammar grammar) {
		var nts = new ArrayList<>(grammar.getNonterminals());
		nts.add(grammar.getStart());
		for (int i = 0; i < nts.size(); ++i) {
			Nonterminal nt1 = nts.get(i);
			if(nt1.getIsRecursive()|| nt1.getIsSelective()) continue;
			for (int j = 0; j < nts.size(); ++j) {
				Nonterminal nt2 = nts.get(j);
				if(nt2.getIsRecursive() || nt2.getIsSelective()) continue;
				for(SententialForm prod1 : nt1.getProductions()) {
					for(SententialForm prod2 : nt2.getProductions()) {
						if(prod1 == prod2) continue;
						int diff = prod2.size() - prod1.size(); 
						if(Math.abs(diff) > 1) continue;
						int mismatchIndex = -1;
						int k=0;
						for(; k< prod1.size(); k++) {
							if(prod2.size() == k && mismatchIndex == -1) {
								mismatchIndex = k;
								break;
							}
							if(prod1.get(k).equals(prod2.get(k + (mismatchIndex!=-1 ? diff : 0) ))) continue;
							else {
								if(mismatchIndex != -1) {
									mismatchIndex = -1;
									break; //this has two many mismatches, gonna stop trying.
								}
								mismatchIndex = k;
								if(diff > 0) k--;
							}
						}
						if(mismatchIndex == -1) continue;
						if(diff > 0) {
							ReplaceIfElse(grammar, prod2 ,prod1, mismatchIndex);					
						}else {
							ReplaceIfElse(grammar, prod1, prod2, mismatchIndex);
						}
						ReplaceAppearances(grammar, nt1, nt2, true);
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

	private static void ReplaceIfElse(Grammar grammar, SententialForm prod1, SententialForm prod2, int mismatchIndex) {
		boolean eqLen = prod2.size() == prod1.size();
		//handle at least one branch
		if(prod1.get(mismatchIndex).getClass() == Nonterminal.class) {
			Nonterminal nt =  (Nonterminal) prod1.get(mismatchIndex);
			if(nt.getIsSelective()) {
				if(eqLen) {
					nt.add(prod2.subList(mismatchIndex, mismatchIndex+1));
					prod2.set(mismatchIndex, nt);
				} else { //gotta check for epsilon
					nt.add(new SententialForm());
					if(prod2.size() == mismatchIndex) {
						prod2.add(nt);
					} else {
						prod2.add(mismatchIndex, nt);
					}
				}
			}
			return;
		}
		//handle two distinct blocks
		Nonterminal newnt = new Nonterminal("C" + ntsCounter++);
		newnt.selective = true;
		newnt.add(new SententialForm(prod1.subList(mismatchIndex, mismatchIndex+1)));
		prod1.set(mismatchIndex, newnt);
		if(eqLen) {
			newnt.add(new SententialForm(prod2.subList(mismatchIndex, mismatchIndex+1)));
			prod2.set(mismatchIndex, newnt);
		} else { //means prod2 is longer, gotta add into prod1
			newnt.add(new SententialForm());
			if(prod2.size() == mismatchIndex) {
				prod2.add(newnt);
			} else {
				prod2.add(mismatchIndex, newnt);
			}
		}
		
		grammar.getNonterminals().add(newnt);
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
		boolean ret;
		do{
			ret = findCondBlocks(grammar);
		} while(ret);
		List<SententialForm> prods = grammar.getStart().getProductions();
		List<SententialForm> sub = prods.subList(0, prods.size() - 1);
		sub.remove(startProd);
		startProd = null;
	}

	private void newStartProd() {
		grammar.getStartProduct().add(new SententialForm());
		startProd = grammar.getCurrStartProduct();
	}

}
