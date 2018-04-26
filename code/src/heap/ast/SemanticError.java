package heap.ast;

@SuppressWarnings("serial")
public class SemanticError extends Error {
	/**
	 * The format string of an error message.
	 */
	public static final String ERROR_MESSAGE = "Semantic error: %s at line %d:%d!";

	public SemanticError(String message, AST n) {
		super(String.format(ERROR_MESSAGE, message, n.line + 1, n.column + 1));
	}
}