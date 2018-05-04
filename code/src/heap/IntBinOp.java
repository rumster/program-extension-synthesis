package heap;

/**
 * The types of binary operations over two integer values.
 * 
 * @author romanm
 */
public enum IntBinOp {
	PLUS, MINUS, TIMES, DIVIDE;

	@Override
	public String toString() {
		switch (this) {
		case PLUS:
			return "+";
		case MINUS:
			return "-";
		case TIMES:
			return "*";
		case DIVIDE:
			return "/";
		default:
			throw new Error("Unexpected case!");
		}
	}
}