package heap;

import java.util.ArrayList;
import java.util.List;

import grammar.*;

/**
 * The operator corresponding to arithmetic addition.
 *
 */
public class PlusExpr extends Node {
	protected List<Node> args = new ArrayList<>(2);

	public PlusExpr(int numOfNonterminals) {
		super(numOfNonterminals);
	}
	
	public PlusExpr(Node lhs, Node rhs) {
		super(lhs.numOfNonterminals + rhs.numOfNonterminals);
		args.add(lhs);
		args.add(rhs);
	}

	protected PlusExpr(List<Node> args) {
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
	public Node clone(List<Node> args) {
		return new PlusExpr(args);
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}
}