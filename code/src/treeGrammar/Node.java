package treeGrammar;

/**
 * Represents a tree node.
 * 
 * @author romanm
 */
public abstract class Node {
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + numOfNonterminals;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Node))
			return false;
		Node other = (Node) obj;
		return numOfNonterminals == other.numOfNonterminals;
	}

	/**
	 * The number of nonterminal leaves in the tree rooted by this node.
	 */
	public final int numOfNonterminals;

	/**
	 * Returns a printable name for this node.
	 */
	public String getName() {
		return getClass().getSimpleName();
	}

	/**
	 * Returns the leftmost nonterminal leaf in the tree rooted by this node.
	 */
	public abstract Nonterminal leftmostNonterminal();

	/**
	 * Constructs a tree node with the indicated number of nonterminal leaves.
	 */
	protected Node(int numOfNonterminals) {
		this.numOfNonterminals = numOfNonterminals;
	}

	/**
	 * Accepts a visitor over this node.
	 * 
	 * @param v
	 *            A derivation tree visitor.
	 */
	public abstract void accept(Visitor v);
}