package heap.ast;

/**
 * A binary conjunction expressions.
 * 
 * @author romanm
 */
public class ASTAndExpr extends ASTBoolExpr {
	public final ASTExpr lhs;
	public final ASTExpr rhs;

	public ASTAndExpr(ASTExpr lhs, ASTExpr rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}
