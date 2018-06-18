package jminor;

import java.util.List;

import grammar.Node;
import grammar.Visitor;

/**
 * The operator corresponding to a disjunctive Boolean connective.
 * 
 * @author romanm
 */
public class OrExpr extends BoolExpr {
	public Node getLhs() {
		return args.get(0);
	}

	public Node getRhs() {
		return args.get(1);
	}

	@Override
	public void accept(Visitor v) {
		JminorVisitor whileVisitor = (JminorVisitor) v;
		whileVisitor.visit(this);
	}

	/**
	 * Constructs the right-hand side of the conjunction rule.
	 */
	public OrExpr(Node lhs, Node rhs) {
		super(lhs, rhs);
	}

	protected OrExpr(List<Node> args) {
		super(args);
		assertNumOfArgs(2);
	}

	@Override
	public OrExpr clone(List<Node> args) {
		assert args.size() == 2;
		return new OrExpr(args);
	}
}