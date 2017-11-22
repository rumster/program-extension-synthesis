package treeGrammar;

/**
 * Computes the weighted sum of two cost functions.
 * 
 * @author romanm
 */
public class CostSum implements CostFun {
	public final CostFun costFun1;
	public final CostFun costFun2;
	public final float weight1;
	public final float weight2;

	/**
	 * Constructs a sum function with both weights 1.
	 */
	public CostSum(CostFun cost1, CostFun cost2) {
		this.costFun1 = cost1;
		this.costFun2 = cost2;
		this.weight1 = 1;
		this.weight2 = 1;
	}

	/**
	 * Constructs a weighted sum from the given cost functions and corresponding
	 * weights.
	 */
	public CostSum(CostFun cost1, float weight1, CostFun cost2, float weight2) {
		this.costFun1 = cost1;
		this.costFun2 = cost2;
		this.weight1 = weight1;
		this.weight2 = weight2;
	}

	public float apply(Node root) {
		float cost1 = costFun1.apply(root);
		float cost2 = costFun2.apply(root);
		return cost1 * weight1 + cost2 * weight2;
	}
}