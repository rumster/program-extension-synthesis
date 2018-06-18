package jminor;

import java.util.List;

import grammar.Node;
import grammar.Visitor;

/**
 * The operator corresponding to an equality comparison between two values.
 * 
 * @author romanm
 */
public class EqExpr extends BoolExpr {
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
	public EqExpr(Node lhs, Node rhs) {
		super(lhs, rhs);
	}

	protected EqExpr(List<Node> args) {
		super(args);
		assertNumOfArgs(2);
	}

	@Override
	public EqExpr clone(List<Node> args) {
		assert args.size() == 2;
		return new EqExpr(args);
	}
}