package heap;

import treeGrammar.*;

/**
 * A terminal representing integer constant.
 *
 */
public class Int extends Terminal {
	public final int value;

	public Int(int value) {
		this.value = value;
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + value;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Int))
			return false;
		Int other = (Int) obj;
		if (value != other.value)
			return false;
		return true;
	}

	public int getValue() {
		return value;
	}
}
