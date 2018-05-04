package heap;

import gp.Domain.Guard;

import java.util.List;

import grammar.Node;

/**
 * The base class of Boolean expressions.
 * 
 * @author romanm
 */
public abstract class BoolExpr extends Expr implements Guard {
	protected BoolExpr(List<Node> nodes) {
		super(nodes);
	}

	protected BoolExpr(Node... nodes) {
		super(nodes);
	}
}