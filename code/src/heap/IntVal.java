package heap;

import grammar.Visitor;

/**
 * A terminal representing an integer value.
 * 
 * @author romanm
 */
public class IntVal extends Val {
	public static final RefType type = new RefType("Int");
	public static final IntVal ZERO = new IntVal(0);

	public final int num;

	public IntVal(int num) {
		this.num = num;
	}

	@Override
	public String toString() {
		return "" + num;
	}

	public String getName() {
		return "i" + num;
	}

	public IntVal add(IntVal o) {
		return new IntVal(num + o.num);
	}

	@Override
	public int hashCode() {
		return num;
	}

	@Override
	public boolean equals(Object o) {
		IntVal other = (IntVal) o;
		return this.num == other.num;
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}
}