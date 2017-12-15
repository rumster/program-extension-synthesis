package heap;

/**
 * The base class of Boolean expressions.
 * 
 * @author romanm
 */
public interface Condition {
	/**
	 * Tests whether the condition holds for the given store.
	 */
	public abstract boolean holds(Store s);
}