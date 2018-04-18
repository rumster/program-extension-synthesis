package heap.ast;

/**
 * A node for associating a value (object or null) with a reference-typed
 * variable.
 * 
 * @author romanm
 */
public class ASTRefVarVal extends ASTVarVal {
	public final String val;

	public ASTRefVarVal(String varName, String val) {
		super(varName);
		this.val = val;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}
