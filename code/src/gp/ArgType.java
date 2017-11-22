package gp;

/**
 * A type that may be associated with arguments.
 * 
 * @author romanm
 */
public final class ArgType {
	public final String name;

	public ArgType(String name) {
		assert name != null;
		this.name = name;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof ArgType && ((ArgType) o).name.equals(this);
	}
}