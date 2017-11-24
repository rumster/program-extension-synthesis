package heap;

import heap.State.Val;
import treeGrammar.Terminal;

/**
 * The base class of fields.
 * 
 * @author romanm
 */
public abstract class Field extends Terminal {
	/**
	 * The type of object containing the field.
	 */
	public final RefType srcType;

	protected final String name;

	/**
	 * The type of the referenced value.
	 */
	protected final Type dstType;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((srcType == null) ? 0 : srcType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Field))
			return false;
		Field other = (Field) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (srcType == null) {
			if (other.srcType != null)
				return false;
		} else if (!srcType.equals(other.srcType))
			return false;
		return true;
	}

	public Field(String name, RefType srcType, Type dstType) {
		this.name = name;
		this.srcType = srcType;
		this.dstType = dstType;
		srcType.add(this);
	}

	public Type getDstType() {
		return dstType;
	}

	@Override
	public final String getName() {
		return name;
	}

	/**
	 * Returns the default value for this type of field.
	 */
	public abstract Val getDefaultVal();
}