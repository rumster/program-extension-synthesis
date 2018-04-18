package heap.ast;

/**
 * A node for a field dereference expression.
 * 
 * @author romanm
 */
public class ASTDerefExpr extends ASTExpr {
	public final ASTExpr lhs;
	public final String fieldName;

	public ASTDerefExpr(ASTExpr lhs, String fieldName) {
		this.lhs = lhs;
		this.fieldName = fieldName;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}