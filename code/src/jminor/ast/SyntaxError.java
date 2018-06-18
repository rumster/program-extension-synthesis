package jminor.ast;

/**
 * Indicates a syntax error encountered during parsing.
 * 
 * @author romanm
 */
@SuppressWarnings("serial")
public class SyntaxError extends Exception {
	/**
	 * The format string of an error message.
	 */
	public static final String ERROR_MESSAGE = "Syntax error: %s at %d:%d!";

	public SyntaxError(String message) {
		super(message);
	}

	public SyntaxError(String message, int line, int column) {
		super(String.format(ERROR_MESSAGE, message, line + 1, column + 1));
	}
}