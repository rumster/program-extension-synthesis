package jminor.ast;

/**
 * Denotes the value of a field for a given objects.
 * 
 * @author romanm
 */
public abstract class ASTFieldVal extends ASTVal {
	public final String fieldName;
	public final String objName;

	public ASTFieldVal(String fieldName, String obj) {
		this.fieldName = fieldName;
		this.objName = obj;
	}
}