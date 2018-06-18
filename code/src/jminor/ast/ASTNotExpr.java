package jminor.ast;

/**
 * A negation expressions.
 * 
 * @author romanm
 */
public class ASTNotExpr extends ASTBoolExpr {
	public final ASTExpr sub;
	
	public ASTNotExpr(ASTExpr sub) {
		this.sub = sub;
	}
	
	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}
