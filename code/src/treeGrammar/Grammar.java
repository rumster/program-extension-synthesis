package treeGrammar;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A tree grammar.
 * 
 * @author romanm
 */
public class Grammar {
	protected final Set<Nonterminal> nonterminals = new LinkedHashSet<>();
	protected final Nonterminal start;

	public Grammar(Nonterminal start) {
		this.start = start;
		nonterminals.add(start);
	}

	public void add(Nonterminal n) {
		nonterminals.add(n);
	}

	public Nonterminal getStart() {
		return start;
	}

	public Set<Nonterminal> getNonterminals() {
		return nonterminals;
	}

	@Override
	public String toString() {
		return Renderer.render(this);
	}
}