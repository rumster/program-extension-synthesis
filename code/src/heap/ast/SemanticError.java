package heap.ast;

@SuppressWarnings("serial")
public class SemanticError extends Error {
	public SemanticError(String message, AST n) {
		super(message + " At " + n.line + ":" + n.column + "!");
	}

	public SemanticError(String message) {
		super(message);
	}
}