package jminor;

/**
 * The types of binary operations over two integer values.
 * 
 * @author romanm
 */
public enum IntBinOp {
	PLUS, MINUS, TIMES, DIVIDE, EQ, LT, LEQ, GT, GEQ;

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
		case LT:
			return "<";
		case EQ:
			return "==";
		case LEQ:
			return "<=";
		case GT:
			return ">";
		case GEQ:
			return ">=";
		default:
			throw new Error("Unexpected case!");
		}
	}
}