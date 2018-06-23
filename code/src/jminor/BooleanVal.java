package jminor;

import bgu.cs.util.treeGrammar.Visitor;

/**
 * Represents a Boolean value.
 * 
 * @author romanm
 */
public class BooleanVal extends Val implements PrimitiveVal {
	public static final BooleanVal TRUE = new BooleanVal(true);
	public static final BooleanVal FALSE = new BooleanVal(false);

	public final boolean val;

	public static BooleanVal get(boolean val) {
		if (val) {
			return TRUE;
		} else {
			return FALSE;
		}
	}

	private BooleanVal(boolean val) {
		this.val = val;
	}

	@Override
	public String toString() {
		return "" + val;
	}

	public String getName() {
		return "" + val;
	}

	@Override
	public int hashCode() {
		if (this == TRUE) {
			return 31;
		} else {
			return 13;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		return obj == this;
	}

	@Override
	public void accept(Visitor v) {
		JminorVisitor whileVisitor = (JminorVisitor) v;
		whileVisitor.visit(this);
	}
}