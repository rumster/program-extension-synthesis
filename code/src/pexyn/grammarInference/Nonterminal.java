package pexyn.grammarInference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jminor.Stmt;
import pexyn.Semantics.Guard;
import pexyn.Semantics.Store;

/**
 * A nonterminal symbol.
 * 
 * @author romanm
 */
public class Nonterminal extends Symbol {
	/**
	 * The set of right-hand sides for this nonterminal.
	 */
	private ArrayList<SententialForm> productions = new ArrayList<>();
	private ArrayList<Guard> guards = new ArrayList<>();
	private final String name;
	boolean recursive = false;
	boolean selective = false;
	boolean ifNt = false;
	boolean ifElseNt = false;
	private ArrayList<Symbol> prodsGen = new ArrayList<>();

	private int matchDepth = 0;

	public Nonterminal(String name) {
		this.name = new String(name);
	}

	public Nonterminal(Nonterminal o) {
		name = new String(o.name);
		productions = new ArrayList<>();
		for (SententialForm sent : o.productions)
			productions.add(new SententialForm(sent));
		subgraphRank = o.subgraphRank;
	}

	public void SetIfElse(SententialForm s1, SententialForm s2){
		assert(productions.isEmpty());
		productions.add(s1);
		productions.add(s2);
		selective = true;
		ifElseNt = true;
	}
	public void SetIf(SententialForm s1){
		assert(productions.isEmpty());
		productions.add(s1);
		productions.add(new SententialForm());
		selective = true;
		ifNt = true;
	}
	
	public void SetRec(List<Symbol> sent){
		productions.clear();
		recursive = true;
		SententialForm s2 = new SententialForm(sent);
		s2.add(this);
		//add(sent);
		add(s2);
		add(new SententialForm()); 
	}
	
	public String getName() {
		return name;
	}

	public List<? extends List<Symbol>> RecBody() {
		assert (recursive);
		Sort();
		var body = productions.get(0).get(0);
		if (body.getClass() == Nonterminal.class) {
			return ((Nonterminal) body).getProductions();
		}
		return Arrays.asList(productions.get(0).subList(0, 1));

	}

	@Override
	public String toString() {
		return name;
	}

	public void Sort() {
		if(productions.isEmpty()) return;
 		productions.sort(productions.get(0).longestProd);
	}

	public void add(List<Symbol> prod) {
		if (!productions.contains(prod)) {
			productions.add(new SententialForm(prod));
			for (Symbol s : prod)
				subgraphRank = Math.max(subgraphRank, s.subgraphRank + 1);
			if (prod.contains(this)) {
				prodsGen.addAll(prod);
				prodsGen.remove(this);
				recursive = true;
			}
		}
	}

	public ArrayList<Symbol> getProductsGenerated() {
		return prodsGen;
	}

	public boolean getIsRecursive() {
		return recursive;
	}

	public boolean getIsSelective() {
		return selective;
	}

	public boolean isIfNt() {
		return ifNt;
	}

	public boolean isIfElseNt() {
		return ifElseNt;
	}
	/**
	 * Returns the set of right-hand sides of the productions associated with this
	 * nonterminal.
	 */
	public List<SententialForm> getProductions() {
		return productions;
	}

	// ! @NOTE only loooks at product #0
	public SententialForm expand() { // doesnt work well with recursion -
										// unused
		SententialForm out = new SententialForm();
		if(productions.isEmpty()) return out;
		SententialForm sent = productions.get(0);
		for (Symbol op : sent) { // for each symbol in the first product
									// alternative
			if (op.getClass() == Nonterminal.class) {
				if (op.equals(this))
					continue;
				Nonterminal opnt = (Nonterminal) op;
				out.addAll(opnt.expand());
			} else {
				out.add(op);
			}
		}
		return out;
	}

	// TODO CONTINUE WORKING ON MATCH: STATE-CAPTURING DOESNT WORK WELL
	public int match(List<?> scope, boolean force) {
		if (matchDepth++ > 100) {
			// some kind of overflow. seems fixed.
			return -1;
		}
		Sort(); // starts with the longest sequence, should catch recursive nt's
				// first.
		boolean matchedAll = true;
		int matchlen = 0;
		for (int i = 0; i < productions.size(); ++i) {
			matchedAll = true;
			SententialForm sent = productions.get(i);
			if (sent.size() == 0 && !force)
				continue;
			matchlen = 0;
			for (Symbol symb : sent) {
				if (symb.getClass() == Nonterminal.class) {
					Nonterminal nt = (Nonterminal) symb;
					if(nt == this && matchlen == 0 ) break; //no support for head recursion
					int subNtMatchLen = nt.match(scope.subList(matchlen, scope.size()), matchlen > 0 || force);
					matchedAll = subNtMatchLen != -1;
					if(!matchedAll) {
						if (matchlen >= scope.size()) {
							matchedAll = false;
							break;
						}
						matchedAll = scope.get(matchlen).equals(nt);
						if(matchedAll) {
							subNtMatchLen = 1;
						}
					}
					/*if(matchedAll && matchlen < scope.size()) {
						var matchedObj = scope.get(matchlen);
						if(matchedObj instanceof Letter) {
							nt.states.add(((Letter)matchedObj).state);
						}
					}*/
					matchlen += subNtMatchLen;
				} else {
					if (matchlen >= scope.size()) {
						matchedAll = false;
						break;
					}
					assert (symb.getClass() == Terminal.class);
					Terminal t = (Terminal) symb;
					var matchedObj = scope.get(matchlen);
					if(matchedObj instanceof Letter) {
						matchedAll = matchedObj.equals(t.id);
						if(matchedAll)
							t.states.add(((Letter)matchedObj).state);
					} else if (matchedObj instanceof Symbol) {
						matchedAll = matchedObj.equals(t);
						if(matchedAll) 
							t.states.addAll(((Symbol)matchedObj).states);
					} else assert(false);
					matchlen++;
				}
				if (!matchedAll) {
					break;
				}
			}

			if (matchedAll) {
				matchDepth--;				
				return matchlen;
			}
		}
		matchDepth--;
		return -1;
	}

	/*
	 * @Override public int hashCode() { final int prime = 31; int result = 1;
	 * result = prime * result + ((name == null) ? 0 : name.hashCode());
	 * ArrayList<SententialForm> tempProds = new ArrayList<>(productions);
	 * for(SententialForm sent : tempProds){ sent.remove(this); } result = prime *
	 * result + ((productions == null) ? 0 : tempProds.hashCode()); return result; }
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Nonterminal other = (Nonterminal) obj;
		if (!name.equals(other.name))
			return false;
		if (productions == null)
			return (other.productions == null);
		if (productions.size() != other.productions.size())
			return false;
		for (int i = 0; i < productions.size(); ++i) {
			SententialForm mySent = productions.get(i);
			SententialForm oSent = other.productions.get(i);
			if (mySent.size() != oSent.size())
				return false;
			for (int j = 0; j < mySent.size(); ++j) {
				if (mySent.get(j) == this) {
					if (oSent.get(j) != other)
						return false;
					else
						continue;
				}
				if (!mySent.get(j).equals(oSent.get(j)))
					return false;
			}
		}
		return true;
	}

	@Override
	public Set<Store> states() {
		if(getIsRecursive()) {
		}
		return null;
	}

	@Override
	Map<Stmt, Set<Store>> FirstStmts() {
		var map = new HashMap<Stmt, Set<Store>>();
		for(SententialForm prod: getProductions()) {
			if(prod.size()>0) {
				Symbol first = prod.get(0);
				map.putAll(first.FirstStmts());
				/*if(first instanceof Nonterminal) {
					Nonterminal ntf = (Nonterminal) first;
					if(ntf.recursive && prod.size()>1) {
						map.putAll(prod.get(1).FirstStmts());
					}
				}*/
			}
		}
		return map;
	}

	public ArrayList<Guard> getGuards() {
		return guards;
	}


}