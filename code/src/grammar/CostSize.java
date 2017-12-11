package grammar;

/**
 * A cost function that returns the number of nodes in the tree.
 * 
 * @author romanm
 */
public class CostSize implements CostFun {
	public static final CostSize v = new CostSize();

	@Override
	public float apply(Node root) {
		float treeSize = size(root);
		return treeSize;
	}

	protected int size(Node n) {
		if (n == null) {
			return 0;
		} else {
			int result = 1;
			for (Node arg : n.getArgs()) {
				result += size(arg);
			}
			return result;
		}
	}
}