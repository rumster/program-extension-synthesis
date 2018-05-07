package heap;

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

		NULL = new Obj(new RefType("nulltype")) {
			@Override
			public String getName() {
				return "NULL";
			}

			@Override
			public String toString() {
				return "NULL";
			}
		};
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
		return id * 31;
	}

	@Override
	public boolean equals(Object obj) {
		Obj other = (Obj) obj;
		return this.id == other.id;
	}
}