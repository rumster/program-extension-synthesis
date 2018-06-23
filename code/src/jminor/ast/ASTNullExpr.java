package jminor.ast;

import jminor.NullType;

/**
 * A node denoting the null value expression.
 * 
 * @author romanm
 */
public class ASTNullExpr extends ASTExpr {
	public static final ASTNullExpr v = new ASTNullExpr();

	static {
		v.setType(NullType.v);
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}

	private ASTNullExpr() {
	}
}