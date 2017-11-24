package heap;

import java.util.ArrayList;
import java.util.List;

import treeGrammar.*;

/**
 * The operator corresponding to arithmetic addition.
 *
 */
public class OpPlus extends InternalNode {
	protected List<Node> args = new ArrayList<>(2);

	public OpPlus(int numOfNonterminals) {
		super(numOfNonterminals);
	}
	
	public OpPlus(Node lhs, Node rhs) {
		super(lhs.numOfNonterminals + rhs.numOfNonterminals);
		args.add(lhs);
		args.add(rhs);
	}

	protected OpPlus(List<Node> args) {
		super(countNonterminals(args));
		assert args.size() == 2 : "Illegal number of arguments for " + getClass().getSimpleName() + ": " + args.size()
				+ "!";
		this.args = args;
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
	public InternalNode clone(List<Node> args) {
		return new OpPlus(args);
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}
}