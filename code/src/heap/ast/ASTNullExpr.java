package heap.ast;

/**
 * A node denoting the null value expression.
 * 
 * @author romanm
 */
public class ASTNullExpr extends ASTExpr {
	public static final ASTNullExpr v = new ASTNullExpr();

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}

	private ASTNullExpr() {
	}
}