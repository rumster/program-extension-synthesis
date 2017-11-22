package gp;

import java.util.Collection;

/**
 * An algorithm for inferring a (deterministic) CFG from a set of plans.
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
public abstract class GlobalCFGGeneralizer<StateType, ActionType, ConditionType> {
	public static enum Result {
		OK, CONDITION_INFERENCE_FAILURE, TIMEOUT
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