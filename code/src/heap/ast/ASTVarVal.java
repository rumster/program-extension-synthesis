package heap.ast;

public abstract class ASTVarVal extends ASTVal {
	public final String varName;
	
	public ASTVarVal(String varName) {
		this.varName = varName;
	}
}
