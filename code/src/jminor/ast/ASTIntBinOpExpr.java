package jminor.ast;

import jminor.IntBinOp;

/**
 * A node for a binary operation over two integer-typed expressions.
 * 
 * @author romanm
 */
public class ASTIntBinOpExpr extends ASTExpr {
	public final IntBinOp op;
	public final ASTExpr lhs;
	public final ASTExpr rhs;

	public ASTIntBinOpExpr(IntBinOp op, ASTExpr lhs, ASTExpr rhs) {
		this.op = op;
		this.lhs = lhs;
		this.rhs = rhs;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}