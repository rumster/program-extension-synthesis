package jminor.ast;

/**
 * A node for a Boolean value.
 * 
 * @author romanm
 */
public class ASTBooleanValExpr extends ASTExpr {
	public final boolean val;

	public ASTBooleanValExpr(boolean val) {
		this.val = val;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}