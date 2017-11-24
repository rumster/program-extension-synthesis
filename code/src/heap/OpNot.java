package heap;

import java.util.ArrayList;
import java.util.List;

import treeGrammar.*;

/**
 * The operator corresponding to a Boolean negation.
 * 
 * TODO: optimize by using a single field for the sub-node.
 * 
 * @author romanm
 */
public class OpNot extends InternalNode {
	protected List<Node> args = new ArrayList<>(1);

	protected OpNot(int numOfNonterminals) {
		super(numOfNonterminals);
	}

	@Override
	public List<Node> getArgs() {
		return args;
	}

	public Node getSub() {
		return args.get(0);
	}

	/**
	 * Constructs the right-hand side of the conjunction rule.
	 */
	public OpNot(Node sub) {
		super(sub.numOfNonterminals);
		args.add(sub);
	}

	protected OpNot(List<Node> args) {
		super(countNonterminals(args));
		assert args.size() == 1 : "Illegal number of arguments for " + getClass().getSimpleName() + ": " + args.size()
				+ "!";
		this.args = args;
	}

	@Override
	public OpNot clone(List<Node> args) {
		return new OpNot(args);
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}
}