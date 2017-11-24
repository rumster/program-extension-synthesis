package gp;

/**
 * A function for ranking control-flow graph. A lower value is better.
 * 
 * @author romanm
 */
public interface CFGRank<ActionType, ConditionType> {
	/**
	 * Computes the rank of the given control-flow graph.
	 */
	public float rank(CFG<ActionType, ConditionType> cfg);
}