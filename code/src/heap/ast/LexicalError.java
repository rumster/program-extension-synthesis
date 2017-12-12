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
		super(message);
		this.line = lineNumber;
	}

	/**
	 * Returns an error message using <code>errorMessage</code> as a format
	 * string and <code>line</code> and <code>ERROR_MESSAGE</code> as the line number
	 * and detailed error message, respectively.
	 */
	public String formatErrorMessage() {
		return String.format(ERROR_MESSAGE, line, getMessage());
	}
}