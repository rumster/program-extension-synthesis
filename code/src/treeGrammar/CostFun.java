package grammar;

/**
 * An interface for cost functions over trees.<br>
 * Cost functions are required to be monotone in the sense that if a derivation
 * tree Y is obtained in one derivation step from a derivation tree X then
 * cost(X) <= cost(Y).
 * 
 * @author romanm
 */
public interface CostFun {
	public static final float INFINITY_COST = Float.POSITIVE_INFINITY;

	/**
	 * Computes the cost of a derivation tree, or INFINITY_COST if the value exceeds
	 * the given bound.
	 * 
	 * @param root
	 *            The root of a derivation tree.
	 * @return A non-negative number.
	 */
	public float apply(Node root);
}