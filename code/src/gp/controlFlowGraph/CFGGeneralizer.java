package gp.controlFlowGraph;

import java.util.Collection;

import gp.Plan;

/**
 * An algorithm for inferring a CFG from a set of plans.
 * 
 * @author romanm
 *
 * @param <StateType>
 *            The type of states over which the CFG executes.
 * @param <ActionType>
 *            The type of actions labeling CFG edges.
 * @param <ConditionType>
 *            The type of conditions on CFG branches.
 */
public abstract class CFGGeneralizer<StateType, ActionType, ConditionType> {
	/**
	 * The possible results of a generalization algorithm.
	 * 
	 * @author romanm
	 */
	public static enum Result {
		OK, CONDITION_INFERENCE_FAILURE, OUT_OF_RESOURCES
	};

	/**
	 * Computes a CFG that can execute each of the given plans when started with
	 * their initial state.
	 * 
	 * @param result
	 *            An empty CFG.
	 * @return The result of trying to generalize the given plans into a valid CFG.
	 */
	public abstract Result generalize(Collection<Plan<StateType, ActionType>> plans,
			CFG<StateType, ActionType, ConditionType> result);
}