package jminor;

import bgu.cs.util.treeGrammar.Visitor;

/**
 * Represents an integer value.
 * 
 * @author romanm
 */
public class IntVal extends Val implements PrimitiveVal {
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
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + num;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		IntVal other = (IntVal) obj;
		if (num != other.num) {
			return false;
		}
		return true;
	}

	@Override
	public void accept(Visitor v) {
		JminorVisitor whileVisitor = (JminorVisitor) v;
		whileVisitor.visit(this);
	}
}