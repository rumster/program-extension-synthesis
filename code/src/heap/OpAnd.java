package heap;

import java.util.ArrayList;
import java.util.List;

import treeGrammar.*;

/**
 * The operator corresponding to a conjunctive Boolean connective.
 * 
 * @author romanm
 */
public class OpAnd extends InternalNode {
	protected List<Node> args = new ArrayList<>(2);

	protected OpAnd(int numOfNonterminals) {
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
	 * Constructs the right-hand side of the conjunction rule.
	 */
	public OpAnd(Node lhs, Node rhs) {
		super(lhs.numOfNonterminals + rhs.numOfNonterminals);
		args.add(lhs);
		args.add(rhs);
	}

	protected OpAnd(List<Node> args) {
		super(countNonterminals(args));
		assert args.size() == 2 : "Illegal number of arguments for " + getClass().getSimpleName() + ": " + args.size()
				+ "!";
		this.args = args;
	}

	@Override
	public OpAnd clone(List<Node> args) {
		return new OpAnd(args);
	}
}