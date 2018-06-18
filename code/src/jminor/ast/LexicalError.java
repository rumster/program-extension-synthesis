package jminor.ast;

/**
 * Indicates a lexical error encountered during scanning.
 * 
 * @author romanm
 */
@SuppressWarnings("serial")
public class LexicalError extends Exception {
	/**
	 * The format string of an error message.
	 */
	public static final String ERROR_MESSAGE = "Lexical error: %s at line %d:%d!";

	public LexicalError(String message, int line, int column) {
		super(String.format(ERROR_MESSAGE, message, line + 1, column + 1));
	}

	/**
	 * This constructor is required to comply with JFlex.
	 */
	public LexicalError(String message) {
		super(message);
	}
}