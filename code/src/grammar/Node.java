package grammar;

import java.util.ArrayList;
import java.util.List;

/**
 * A node in a derivation tree.
 * 
 * @author romanm
 */
public abstract class Node {
	/**
	 * An indexed list of children nodes.
	 */
	public abstract List<Node> getArgs();

	/**
	 * The number of nonterminal leaves in the tree rooted by this node.
	 */
	public final int numOfNonterminals;

	public static int countNonterminals(List<? extends Node> nodes) {
		int result = 0;
		for (Node n : nodes) {
			result += n.numOfNonterminals;
		}
		return result;
	}

	/**
	 * Accepts a visitor.
	 */
	public abstract void accept(Visitor v);

	/**
	 * Functionally applies the production represented by the given derivation tree
	 * to the leftmost nonterminal leaf.
	 * 
	 * @param rhs
	 *            The right-hand side of a production.
	 * @return The transformed operator, or null if no nonterminals exist.
	 */
	public Node substituteLeftmostNonterminal(Node op) {
		if (numOfNonterminals == 0) {
			return this;
		}

		boolean handledLeftmost = false;
		ArrayList<Node> newArgs = new ArrayList<>();
		for (Node arg : getArgs()) {
			if (arg.numOfNonterminals == 0) {
				newArgs.add(arg);
				continue;
			}
			if (handledLeftmost) {
				newArgs.add(arg);
				continue;
			}

			if (arg instanceof Nonterminal) {
				newArgs.add(op);
			} else {
				Node argOp = (Node) arg;
				Node result = argOp.substituteLeftmostNonterminal(op);
				newArgs.add(result);
			}
			handledLeftmost = true;
		}
		Node result = this.clone(newArgs);
		return result;
	}

	/**
	 * Returns the leftmost nonterminal, if one exists, and null otherwise.
	 */
	public Nonterminal leftmostNonterminal() {
		if (numOfNonterminals == 0) {
			return null;
		}

		for (Node arg : getArgs()) {
			if (arg.numOfNonterminals > 0) {
				return arg.leftmostNonterminal();
			}
		}
		return null;
	}

	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getArgs() == null) ? 0 : getArgs().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Node))
			return false;
		Node other = (Node) obj;
		return getArgs().equals(other.getArgs());
	}

	/**
	 * Creates an tree just like this one but with the given list of arguments.
	 * 
	 * @param args
	 *            The arguments of the returned operator.
	 */
	public abstract Node clone(List<Node> args);

	/**
	 * Sums the number of nonterminals in all the given trees.
	 */
	protected static int sumNumOfNonterminals(List<Node> args) {
		int result = 0;
		for (Node n : args) {
			result += n.numOfNonterminals;
		}
		return result;
	}

	/**
	 * Constructs a tree node with the indicated number of nonterminal leaves.
	 */
	protected Node(int numOfNonterminals) {
		this.numOfNonterminals = numOfNonterminals;
	}
}