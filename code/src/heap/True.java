package heap;

import java.util.List;

import grammar.Node;
import grammar.Visitor;

/**
 * Represents the always-true Boolean expression.
 * 
 * @author romanm
 */
public class True extends BoolExpr {
	public static True v = new True();

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}

	@Override
	public Node clone(List<Node> args) {
		return this;
	}

	@Override
	public String toString() {
		return "true";
	}

	private True() {
	}
}