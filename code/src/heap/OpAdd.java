package heap;

import java.util.ArrayList;
import java.util.List;

import treeGrammar.*;

/**
 * The operator corresponding to an addition of two integer values.
 * 
 * @author romanm
 */
public class OpAdd extends InternalNode {
	protected List<Node> args = new ArrayList<>(2);

	protected OpAdd(int numOfNonterminals) {
		super(numOfNonterminals);
	}

	@Override
	public List<Node> getArgs() {
		return args;
	}

	public Node getLhs() {
		return args.get(0);
	}

	public Node getRhs() {
		return args.get(1);
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}

	/**
	 * Constructs the right-hand side of an equality comparison.
	 */
	public OpAdd(Node lhs, Node rhs) {
		super(lhs.numOfNonterminals + rhs.numOfNonterminals);
		args.add(lhs);
		args.add(rhs);
	}

	protected OpAdd(List<Node> args) {
		super(countNonterminals(args));
		assert args.size() == 2 : "Illegal number of arguments for " + getClass().getSimpleName() + ": " + args.size()
				+ "!";
		this.args = args;
	}

	@Override
	public OpAdd clone(List<Node> args) {
		return new OpAdd(args);
	}
}