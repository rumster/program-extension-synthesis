package heap.ast;

/**
 * The base class of abstract syntax tree nodes.
 * 
 * @author romanm
 */
public abstract class AST {
	public static String INT_TYPE_NAME = "int";
	public static String NULL_VAL_NAME = "null";	

	/**
	 * Implements a part of the visitor pattern.
	 */
	public abstract void accept(Visitor v);
}