package heap.ast;

/**
 * A node for an assignment statement.
 * 
 * @author romanm
 */
public class ASTAssign extends ASTStmt {
	public final ASTExpr lhs;
	public final ASTExpr rhs;

	public ASTAssign(ASTExpr lhs, ASTExpr rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}
