package heap;

import grammar.Visitor;

/**
 * A terminal representing an integer value.
 * 
 * @author romanm
 */
public class IntVal extends Val {
	public static final RefType type = new RefType("Int");
	public static final IntField field = new IntField("val", type);
	public static final IntVal ZERO = new IntVal(0);

	public final int num;

	public IntVal(int num) {
		this.num = num;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof IntVal))
			return false;
		IntVal other = (IntVal) o;
		return this.num == other.num;
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
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + num;
		return result;
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}
}