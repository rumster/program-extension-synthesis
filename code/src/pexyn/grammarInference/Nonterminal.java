package pexyn.grammarInference;

import java.util.ArrayList;
import java.util.List;

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
		for(SententialForm sent : o.productions)
			productions.add(new SententialForm(sent));
		subgraphRank = o.subgraphRank;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}
	public List<SententialForm> RecBody() {
		assert(recursive);
		return ((Nonterminal)productions.get(1).get(0)).getProductions();
	}

	@Override
	public String toString() {
		return name;
	}

	public void Sort() {
		productions.sort(productions.get(0).longestProd); // ! TODO fix this to
															// be static
	}

	public void add(List<Symbol> prod) {
		if(!productions.contains(prod)) {
			productions.add(new SententialForm(prod));
			for(Symbol s: prod) subgraphRank= Math.max(subgraphRank, s.subgraphRank + 1);
			if(prod.contains(this)){
				prodsGen.addAll(prod);
				prodsGen.remove(this);
				recursive = true;
			}
		}
	}

	public ArrayList<Symbol> getProductsGenerated(){
		return prodsGen;
	}
	public boolean getIsRecursive(){
		return recursive;
	}

	public boolean getIsSelective() {
		return selective;
	}
	/**
	 * Returns the set of right-hand sides of the productions associated with
	 * this nonterminal.
	 */
	public List<SententialForm> getProductions() {
		return productions;
	}

	//! @NOTE only loooks at product #0
	public SententialForm expand() { // doesnt work well with recursion -
										// unused
		SententialForm out = new SententialForm();
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
	
	public int match(List<? extends Letter> scope, boolean force) {
		if(matchDepth++ > 100) {
			//some kind of overflow. check it out.
			System.out.println("stuck matching");
			System.out.println(this.toString());
			System.out.println(scope.toString());
			System.out.println(force);
			
		}
		Sort(); // starts with the longest sequence, should catch recursive nt's
				// first.
		boolean matchedAll = true;
		int matchlen = 0;
		for (int i = 0; i < productions.size(); ++i) {
			matchedAll = true;
			SententialForm sent = productions.get(i);
			if(sent.size() == 0 && !force) continue;
			matchlen = 0;
			for (Symbol symb : sent) {
				if (matchlen >= scope.size()) {
					// this means we havent finished the nt, better choose a
					// different prod
					matchedAll = false;
					break;
				}
				;
				if (symb.getClass() == Nonterminal.class) {
					Nonterminal nt = (Nonterminal) symb;
					int subNtMatchLen = nt.match(scope.subList(matchlen, scope.size()), matchlen > 0);
					matchedAll = subNtMatchLen != -1;
					matchlen += subNtMatchLen;
				} else {
					assert (symb.getClass() == Terminal.class);
					Terminal t = (Terminal) symb;
					matchedAll = scope.get(matchlen).equals(t.id);
					matchlen++;
				}
				if (!matchedAll) {
					break;
				}
			}

			matchDepth--;
			if (matchedAll)
				return matchlen;
		}
		matchDepth--;
		return  -1;
	}

	/*
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		ArrayList<SententialForm> tempProds = new ArrayList<>(productions);
		for(SententialForm sent : tempProds){
			sent.remove(this);
		}
		result = prime * result + ((productions == null) ? 0 : tempProds.hashCode());
		return result;
	}
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
		if(!name.equals(other.name)) return false;
		if (productions == null) 
			return (other.productions == null);
		if(productions.size() != other.productions.size()) return false;
		for(int i=0; i< productions.size(); ++i){
			SententialForm mySent = productions.get(i);
			SententialForm oSent = other.productions.get(i);
			if(mySent.size() != oSent.size()) return false;
			for(int j=0; j< mySent.size(); ++j){
				if(mySent.get(j) == this){
					if(oSent.get(j) != other)
						return false;
					else
						continue;
				}
				if(!mySent.get(j).equals(oSent.get(j))) return false;
			}
		}
		return true;
	}



}