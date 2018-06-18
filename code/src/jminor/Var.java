package jminor;

import grammar.Token;

/**
 * The base class of variables.
 * 
 * @author romanm
 */
public abstract class Var extends Token {
	public final String name;

	public final VarRole role;

	/**
	 * Indicates whether or not this variable may be assigned to.
	 */
	public final boolean readonly;

	protected final Type type;

	/**
	 * The intended purpose of a variable within a procedure.
	 * 
	 * @author romanm
	 */
	public static enum VarRole {
		/**
		 * A procedure input/output argument.
		 */
		ARG,

		/**
		 * A temporary variable.
		 */
		TEMP
	}

	public Var(String name, Type type, VarRole role, boolean out, boolean readonly) {
		this.name = name;
		this.type = type;
		this.role = role;
		this.readonly = readonly;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Var other = (Var) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public Type getType() {
		return type;
	}

	public boolean isReadonly() {
		return readonly;
	}

	@Override
	public final String getName() {
		return name;
	}

	@Override
	public final String toString() {
		return name + ":" + getType().name;
	}
}