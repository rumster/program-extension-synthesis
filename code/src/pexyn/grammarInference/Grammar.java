package pexyn.grammarInference;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A context-free grammar.
 * 
 * @author romanm
 *
 * @param The
 *            type of an alphabet letter.
 */
public class Grammar {
	private Nonterminal start;
	private ArrayList<Nonterminal> nonterminals = new ArrayList<>();

	private final Comparator<Nonterminal> longestProd = new Comparator<Nonterminal>() {
		//! @note only works on level1 recursion.
		public int compare(Nonterminal nt1, Nonterminal nt2) {/*
			for (SententialForm op : nt1.getProductions()) {
				for (Symbol s : op) {
					if (s.equals(nt2))
						return -1000;
				}
			}
			for (SententialForm op : nt2.getProductions()) {
				for (Symbol s : op) {
					if (s.equals(nt1))
						return 1000;
				}
			}

			int size1 = nt1.expand().size(), size2 = nt2.expand().size();
			return size2 - size1;*/
			int diff = nt2.subgraphRank - nt1.subgraphRank;
			if(diff == 0) {
				int size1 = nt1.expand().size(), size2 = nt2.expand().size();
				return size2 - size1;
			}
			return diff;
		}
		
	};

	public void Sort() {
		nonterminals.sort(longestProd);
	}

	public Grammar() {
		clear();
	}

	public Grammar(Grammar o) {
		start = new Nonterminal(o.getStart());
		nonterminals = new ArrayList<Nonterminal>();
		for(Nonterminal nt: o.getNonterminals()) {
			Nonterminal newnt = new Nonterminal(nt);
			nonterminals.add(newnt);
		}
		for(Nonterminal nt: nonterminals) {
			for(SententialForm prod : nt.getProductions()) {
				prod.replaceAll( (Symbol symb) -> {
					if(symb.getClass() == Nonterminal.class) {
						for(Nonterminal nt2: nonterminals) { 
							if(nt2.getName().equals(((Nonterminal) symb).getName()))
								return nt2;
						}
					}
					return symb;
				});
			}
		}
	}

	/**
	 * Adds a nonterminal to this grammar.
	 */
	public void add(Nonterminal n) {
		assert !nonterminals.contains(n);
		nonterminals.add(n);
	}

	public Nonterminal getStart() {
		assert start != null;
		return start;
	}

	public void setStart(Nonterminal s) {
		assert s != null;
		start = new Nonterminal(s);
	}

	public void setStart(SententialForm s) {
		start.getProductions().clear();
		start.add(s.copy());
	}

	/**
	 * Returns the set of nonterminals appearing in this grammar.
	 */
	public ArrayList<Nonterminal> getNonterminals() {
		return nonterminals;
	}
	
	/**
	 * Returns the set of products appearing in the start nt of the grammar.
	 */
	public List<SententialForm> getStartProduct() {
		return start.getProductions();
	}	
	
	/**
	 * Returns the product appearing in the current-build start production of the grammar.
	 */
	public SententialForm getCurrStartProduct() {
		return getStartProduct().get(getStartProduct().size() - 1);
	}

	public void clear() {
		this.start = new Nonterminal("Start");
		nonterminals.clear();

	}

	@Override
	public String toString() {
		return Renderer.render(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nonterminals == null) ? 0 : nonterminals.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Grammar other = (Grammar) obj;
		if (nonterminals == null) {
			if (other.nonterminals != null)
				return false;
		} else if (!nonterminals.equals(other.nonterminals))
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		return true;
	}
}