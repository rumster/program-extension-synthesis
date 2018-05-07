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
		int result = name.hashCode();
		result = result * prime + srcType.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		Field other = (Field) obj;
		return this.name.equals(other.name) && this.srcType.equals(other.srcType);
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