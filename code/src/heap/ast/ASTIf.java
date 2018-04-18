package heap.ast;

/**
 * A node for a condition statement.
 * 
 * @author romanm
 */
public class ASTIf extends ASTStmt {
	public final ASTExpr cond;
	public final ASTStmt thenStmt;
	public final ASTStmt elseStmt;

	public ASTIf(ASTExpr cond, ASTStmt thenStmt) {
		this.cond = cond;
		this.thenStmt = thenStmt;
		this.elseStmt = null;
	}
	
	public ASTIf(ASTExpr cond, ASTStmt thenStmt, ASTStmt elseStmt) {
		this.cond = cond;
		this.thenStmt = thenStmt;
		this.elseStmt = elseStmt;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}