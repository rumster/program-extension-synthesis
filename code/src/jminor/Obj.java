package jminor;

/**
 * A heap object.
 * 
 * @author romanm
 */
public class Obj extends Val {
	private static int counter = 0;

	/**
	 * The constant null objects.
	 */
	public static final Obj NULL;

	static {
		counter = 0;

		NULL = new Obj(NullType.v);
	}

	/**
	 * The type of this object.
	 */
	public final RefType type;

	/**
	 * The "address" of this objects.
	 */
	private final int id;

	public Obj(RefType type) {
		this.type = type;
		this.id = counter;
		++counter;
	}

	@Override
	public String toString() {
		return type + "#" + id;
	}

	@Override
	public String getName() {
		return toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + id;
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
		Obj other = (Obj) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}
}