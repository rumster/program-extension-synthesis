package heap;

/**
 * A heap object.
 * 
 * @author romanm
 */
public class Obj extends Val {
	/**
	 * The constant null objects.
	 */
	public static final Obj NULL = new Obj(new RefType("nulltype")) {
		@Override
		public String getName() {
			return "NULL";
		}

		@Override
		public String toString() {
			return "NULL";
		}
	};

	/**
	 * The type of this object.
	 */
	public final RefType type;

	/**
	 * The "address" of this objects.
	 */
	private final int id;

	private static int counter = 0;

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
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}
}