package heap;

import grammar.Token;

/**
 * The base class of variables.
 * 
 * @author romanm
 */
public abstract class Var extends Token {
	public final VarRole role;

	/**
	 * Indicates whether this variable, which should be an argument, is live when
	 * the synthesized program terminates.
	 */
	public final boolean out;

	/**
	 * Indicates whether or not this variable may be assigned to.
	 */
	public final boolean readonly;

	public final String name;
	
	protected final Type type;

	public static enum VarRole {
		ARG, TEMPORARY
	}

	public Var(String name, Type type, VarRole role, boolean out, boolean readonly) {
		this.name = name;
		this.type = type;
		this.role = role;
		this.out = out;
		this.readonly = readonly;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (out ? 1231 : 1237);
		result = prime * result + (readonly ? 1231 : 1237);
		result = prime * result + ((role == null) ? 0 : role.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Var))
			return false;
		Var other = (Var) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (out != other.out)
			return false;
		if (readonly != other.readonly)
			return false;
		if (role != other.role)
			return false;
		return true;
	}
	
	public Type getType() {
		return type;
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