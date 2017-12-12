package heap.ast;

/**
 * Indicates a syntax error encountered during parsing.
 * 
 * @author romanm
 */
@SuppressWarnings("serial")
public class SyntaxError extends Exception {
	private int line;

	/**
	 * The format string of an error message.
	 */
	public static final String ERROR_MESSAGE = "Syntax error in line %d: %s!";

	public SyntaxError(String message) {
		super(message);
	}

	public SyntaxError(String message, int lineNumber) {
		super(message);
		this.line = lineNumber;
	}

	/**
	 * Returns an error message using <code>ERROR_MESSAGE</code> as a format
	 * string and <code>line</code> and <code>message</code> as the line number
	 * and detailed error message, respectively.
	 */
	public String formatErrorMessage() {
		return String.format(ERROR_MESSAGE, line, getMessage());
	}
}