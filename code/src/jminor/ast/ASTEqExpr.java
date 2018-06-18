package jminor.ast;

/**
 * An equality expression.
 * 
 * @author romanm
 */
public class ASTEqExpr extends ASTBoolExpr {
	public final ASTExpr lhs;
	public final ASTExpr rhs;
	
	public ASTEqExpr(ASTExpr lhs, ASTExpr rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}
	
	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}
