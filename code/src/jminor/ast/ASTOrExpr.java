package jminor.ast;

/**
 * A binary disjunction expressions.
 * 
 * @author romanm
 */
public class ASTOrExpr extends ASTBoolExpr {
	public final ASTExpr lhs;
	public final ASTExpr rhs;
	
	public ASTOrExpr(ASTExpr lhs, ASTExpr rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}
	
	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}
