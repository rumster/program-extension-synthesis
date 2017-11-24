package treeGrammar;

import java.util.ArrayList;
import java.util.List;

/**
 * An operator is like a logical term and serves three roles:<br>
 * 1. It represents the right-hand side of a grammar production, in which case
 * its arguments are terminals and nonterminals;<br>
 * 2. It represent a node in the derivation tree corresponding to its associated
 * production, in which case its arguments are terminals, nonterminals, and
 * other operator instances; and<br>
 * 3. Terminals are represented by nullary operators.
 * 
 * @author romanm
 */
public abstract class InternalNode extends Node {
	public abstract List<Node> getArgs();

	public static int countNonterminals(List<? extends Node> nodes) {
		int result = 0;
		for (Node n : nodes) {
			result += n.numOfNonterminals;
		}
		return result;
	}

	public static int countNonterminals(Node n1, Node n2) {
		return n1.numOfNonterminals + n2.numOfNonterminals;
	}

	public static int countNonterminals(Node n1, Node n2, Node n3) {
		return n1.numOfNonterminals + n2.numOfNonterminals + n3.numOfNonterminals;
	}

	/**
	 * Creates an operator just like this one but with the given list of arguments.
	 * 
	 * @param args
	 *            The arguments of the returned operator.
	 * @return An operator.
	 */
	public abstract InternalNode clone(List<Node> args);

	/**
	 * Accepts a visitor.
	 */
	public abstract void accept(Visitor v);

	/**
	 * Functionally applies the production represented by the given operator to the
	 * leftmost nonterminal leaf.
	 * 
	 * TODO: consider applying hash-consing.
	 * 
	 * @param op
	 *            An operator associated with the given nonterminal.
	 * @return The transformed operator, or null if no nonterminals exist.
	 */
	public InternalNode substituteLeftmost(InternalNode op) {
		if (numOfNonterminals == 0)
			return this;

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
				InternalNode argOp = (InternalNode) arg;
				InternalNode result = argOp.substituteLeftmost(op);
				newArgs.add(result);
			}
			handledLeftmost = true;
		}
		InternalNode result = this.clone(newArgs);
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

	@Override
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
		if (!(obj instanceof InternalNode))
			return false;
		InternalNode other = (InternalNode) obj;
		return getArgs().equals(other.getArgs());
	}

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

	protected InternalNode(int numOfNonterminals) {
		super(numOfNonterminals);
	}
}