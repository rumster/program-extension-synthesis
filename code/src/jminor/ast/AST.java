package jminor.ast;

/**
 * The base class of abstract syntax tree nodes.
 * 
 * @author romanm
 */
public abstract class AST {
	public static String INT_TYPE_NAME = "int";
	public static String CHAR_TYPE_NAME = "char";
	public static String LONG_TYPE_NAME = "long";
	public static String FLOAT_TYPE_NAME = "float";
	public static String DOUBLE_TYPE_NAME = "double";
	public static String BOOLEAN_TYPE_NAME = "boolean";	
	public static String NULL_VAL_NAME = "null";
	
	public int line;
	public int column;

	/**
	 * Implements a part of the visitor pattern.
	 */
	public abstract void accept(Visitor v);
}