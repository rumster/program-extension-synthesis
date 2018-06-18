package jminor.ast;

/**
 * A variable expression.
 * 
 * @author romanm
 */
public class ASTVarExpr extends ASTExpr {
	public final String varName;

	public ASTVarExpr(String varName) {
		this.varName = varName;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}