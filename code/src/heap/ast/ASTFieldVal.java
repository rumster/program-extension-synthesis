package heap.ast;

public abstract class ASTFieldVal extends AST {
	public final String fieldName;
	public final String obj;

	public ASTFieldVal(String fieldName, String obj) {
		this.fieldName = fieldName;
		this.obj = obj;
	}
}