package jminor.ast;

/**
 * A node for a while statement.
 * 
 * @author romanm
 */
public class ASTWhile extends ASTStmt {
	public final ASTExpr cond;
	public final ASTStmt body;

	public ASTWhile(ASTExpr cond, ASTStmt body) {
		this.cond = cond;
		this.body = body;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}