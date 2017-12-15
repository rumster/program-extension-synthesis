package heap.ast;

public abstract class ASTFieldVal extends ASTVal {
	public final String fieldName;
	public final String objName;

	public ASTFieldVal(String fieldName, String obj) {
		this.fieldName = fieldName;
		this.objName = obj;
	}
}