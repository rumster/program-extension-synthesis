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
}