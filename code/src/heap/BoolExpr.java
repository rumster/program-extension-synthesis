package heap;

import java.util.List;

import grammar.Node;

/**
 * The base class of Boolean expressions.
 * 
 * @author romanm
 */
public abstract class BoolExpr extends Expr {
	/**
	 * Tests whether the condition holds for the given store.
	 */
	public boolean satisfies(Store s) {
		throw new UnsupportedOperationException("Change this to a call to PWhileInterpreter!");
	}

	protected BoolExpr(List<Node> nodes) {
		super(nodes);
	}

	protected BoolExpr(Node... nodes) {
		super(nodes);
	}
}