package heap;

import treeGrammar.Terminal;

/**
 * The base class of variables.
 * 
 * @author romanm
 */
public abstract class Var extends Terminal {
	public final VarRole role;

	/**
	 * Indicates whether this variable, which should be an argument, is live
	 * when the synthesized program terminates.
	 */
	public final boolean out;

	/**
	 * Indicates whether or not this variable may be assigned to.
	 */
	public final boolean readonly;

	protected final String name;

	public static enum VarRole {
		ARG, TEMPORARY
	}

	public Var(String name, VarRole role, boolean out, boolean readonly) {
		this.name = name;
		this.role = role;
		this.out = out;
		this.readonly = readonly;
	}

	public Var(String name) {
		this(name, VarRole.TEMPORARY, false, false);
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

	@Override
	public final String getName() {
		return name;
	}
}