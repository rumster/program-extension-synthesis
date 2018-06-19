package grammar;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A context-free grammar.
 * 
 * @author romanm
 */
public class Grammar {
	public final Nonterminal start;

	/**
	 * A grammar is made of a set of nonterminals, each of which maintains its own
	 * set of productions.
	 */
	protected final Set<Nonterminal> nonterminals = new LinkedHashSet<>();

	public Grammar(Nonterminal start) {
		this.start = start;
		nonterminals.add(start);
	}

	public void add(Nonterminal n) {
		nonterminals.add(n);
	}

	public Set<Nonterminal> nonterminals() {
		return nonterminals;
	}

	@Override
	public String toString() {
		return Renderer.render(this);
	}
}