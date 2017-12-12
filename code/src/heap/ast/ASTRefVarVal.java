package heap.ast;

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
