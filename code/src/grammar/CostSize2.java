package grammar;

import heap.IntField;

/**
 * A cost function that returns the number of states in the tree.
 * 
 * @author romanm
 */
public class CostSize2 implements CostFun {
	public static final CostSize2 v = new CostSize2();

	@Override
	public float apply(Node root) {
		float treeSize = size(root);
		return treeSize;
	}

	protected int size(Node n) {
		if(n == null){
			return 0;
		} else if(n instanceof IntField) {
			return -1;
		} else {
			int result = 1;
			for (Node arg : n.getArgs()) {
				result += size(arg);
			}
			return result;
		}
	}
	
	/*
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
	*/
}
