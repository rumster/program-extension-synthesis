package grammar;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import heap.Renderer;

/**
 * A node in a derivation tree.
 * 
 * @author romanm
 */
public abstract class Node {
	/**
	 * Returns the list of children nodes. The implementation uses reflection.
	 */
	public List<Node> getArgs() {
		ArrayList<Node> result = new ArrayList<>(2);
		Field[] thisClassFields = Node.class.getFields();
		for (Field field : getClass().getFields()) {
			// Ignore any field declared in this class. We only care about subclass fields.
			if (Arrays.binarySearch(thisClassFields, field) >= 0) {
				continue;
			}
			Node arg;
			try {
				arg = (Node) field.get(this);
				result.add(arg);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * The number of nonterminal leaves in the tree rooted by this node.
	 */
	final int numOfNonterminals;
	
	public boolean concrete() {
		return numOfNonterminals == 0;
	}

	public static int countNonterminals(Iterable<Node> nodes) {
		int result = 0;
		for (Node n : nodes) {
			result += n.numOfNonterminals;
		}
		return result;
	}

	public static int countNonterminals(Node... nodes) {
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
		int result = 1;
		result = prime * result + ((getArgs() == null) ? 0 : getArgs().hashCode());
		return result;
	}

	@Override	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
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

	public void assertNumOfArgs(int num) {
		assert getArgs().size() == 2 : "Illegal number of arguments for " + getClass().getSimpleName() + ": "
				+ getArgs().size() + "!";
	}

	/**
	 * Constructs a tree node with the indicated number of nonterminal leaves.
	 */
	protected Node(int numOfNonterminals) {
		this.numOfNonterminals = numOfNonterminals;
	}

	/**
	 * Constructs a tree node with 0 leaves.
	 */
	protected Node() {
		this(0);
	}
	
	@Override
	public String toString() {
		return Renderer.render(this);
	}
}