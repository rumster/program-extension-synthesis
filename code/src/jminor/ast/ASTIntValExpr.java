package jminor.ast;

/**
 * A node for a constant integer value.
 * 
 * @author romanm
 */
public class ASTIntValExpr extends ASTExpr {
	public final int val;

	public ASTIntValExpr(int val) {
		this.val = val;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}