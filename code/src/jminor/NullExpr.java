package jminor;

import java.util.List;

import grammar.Node;
import grammar.Visitor;

/**
 * The null value.
 * 
 * @author romanm
 */
public class NullExpr extends Expr {
	public static final NullExpr v = new NullExpr();

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}

	@Override
	public Node clone(List<Node> args) {
		assertNumOfArgs(0);
		return this;
	}

	@Override
	public boolean equals(Object o) {
		return o == this;
	}

	private NullExpr() {
	}
}