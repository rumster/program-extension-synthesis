package jminor.ast;

/**
 * A node for associating a value with a variable.
 * 
 * @author romanm
 */
public abstract class ASTVarVal extends ASTVal {
	public final String varName;

	public ASTVarVal(String varName) {
		this.varName = varName;
	}
}
