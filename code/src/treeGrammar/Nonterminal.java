package treeGrammar;

import java.util.ArrayList;
import java.util.List;

/**
 * A nonterminal symbol, associated with a list of productions.
 * 
 * @author romanm
 */
public class Nonterminal extends Node {
	public final String name;
	protected final List<InternalNode> productions = new ArrayList<>();
	private static int counter;

	public Nonterminal(String name) {
		super(1);
		assert name != null;
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Nonterminal)) {
			return false;
		}
		Nonterminal other = (Nonterminal) obj;
		return this.name.equals(other.name);
	}

	public void add(InternalNode op) {
		productions.add(op);
	}

	@Override
	public Nonterminal leftmostNonterminal() {
		return this;
	}

	public List<InternalNode> getProductions() {
		return productions;
	}

	protected Nonterminal(int numOfNonterminals) {
		super(numOfNonterminals);
		this.name = "N" + counter;
		++counter;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}