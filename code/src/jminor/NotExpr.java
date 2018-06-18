package jminor;

import java.util.List;

import grammar.Node;
import grammar.Visitor;

/**
 * The operator corresponding to a Boolean negation.
 * 
 * TODO: optimize by using a single field for the sub-node.
 * 
 * @author romanm
 */
public class NotExpr extends BoolExpr {
	public Node getSub() {
		return args.get(0);
	}

	/**
	 * Constructs the right-hand side of the conjunction rule.
	 */
	public NotExpr(Node sub) {
		super(sub);
	}

	protected NotExpr(List<Node> args) {
		super(args);
		assertNumOfArgs(1);
	}

	@Override
	public NotExpr clone(List<Node> args) {
		return new NotExpr(args);
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}
}