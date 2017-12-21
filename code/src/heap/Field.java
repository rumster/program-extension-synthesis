package heap;

import grammar.Token;

/**
 * The base class of fields.
 * 
 * @author romanm
 */
public abstract class Field extends Token {
	public final String name;

	/**
	 * The type of object containing the field.
	 */
	public final RefType srcType;

	/**
	 * The type of the referenced value.
	 */
	public final Type dstType;

	public final boolean ghost;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		if (!(obj instanceof Field)) {
			return false;
		}
		Field other = (Field) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name))
			return false;
		if (srcType == null) {
			if (other.srcType != null) {
				return false;
			}
		} else if (srcType != other.srcType) {
			return false;
		}
		return true;
	}

	public Field(String name, RefType srcType, Type dstType, boolean ghost) {
		this.name = name;
		this.srcType = srcType;
		this.dstType = dstType;
		this.ghost = ghost;
		srcType.add(this);
	}

	/**
	 * Returns the default value for this type of field.
	 */
	public abstract Val getDefaultVal();
}