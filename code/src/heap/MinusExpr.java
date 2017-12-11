package heap;

import java.util.ArrayList;
import java.util.List;

import grammar.*;

/**
 * The operator corresponding to arithmetic negation (unary operator).
 *
 */
public class MinusExpr extends Node {
	protected List<Node> args = new ArrayList<>(1);

	public MinusExpr(int numOfNonterminals) {
		super(numOfNonterminals);
	}

	public MinusExpr(Node sub) {
		super(sub.numOfNonterminals);
		args.add(sub);
	}

	@Override
	public List<Node> getArgs() {
		return args;
	}

	public Node getSub() {
		return args.get(0);
	}

	protected MinusExpr(List<Node> args) {
		super(countNonterminals(args));
		assert args.size() == 1 : "Illegal number of arguments for " + getClass().getSimpleName() + ": " + args.size()
				+ "!";
		this.args = args;
	}

	@Override
	public Node clone(List<Node> args) {
		return new MinusExpr(args);
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}
}