package heap.ast;

/**
 * Indicates a lexical error encountered during scanning.
 * 
 * @author romanm
 */
@SuppressWarnings("serial")
public class LexicalError extends Exception {
	protected int line;

	/**
	 * The format string of an error message.
	 */
	public static final String ERROR_MESSAGE = "Lexical error in line %d: %s!";

	public LexicalError(String message) {
		super(message);
	}

	public LexicalError(String message, int lineNumber) {
		super(String.format(ERROR_MESSAGE, lineNumber, message));
		this.line = lineNumber;
	}
}