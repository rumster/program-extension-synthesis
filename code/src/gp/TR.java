package gp;

import java.util.Collection;

/**
 * A non-deterministic transition relation with a positive weight associated
 * with each transition.
 * 
 * @author romanm
 *
 * @param <StateType>
 *            The type of states in the state space.
 * @param <ActionType>
 *            The type of actions in the transition relation.
 */
public interface TR<StateType, ActionType> {
	/**
	 * A convenient value for the maximum possible cost.
	 */
	public static final float MAX_COST = Float.MAX_VALUE;

	/**
	 * Returns the set of actions that are enabled for the given state.
	 */
	public Collection<ActionType> enabledActions(StateType state);

	/**
	 * Returns the cost of taking the transition from the source state to the
	 * destination state with the given action.
	 */
	public float transitionCost(StateType src, ActionType action, StateType dst);

	/**
	 * Applies the given action to the given state and returns the resulting
	 * (possibly empty) collection of states.
	 */
	public Collection<StateType> apply(StateType state, ActionType action);

	/**
	 * Returns the maximal cost of taking a transition from the source state to any
	 * destination state with the given action.
	 */
	public default float actionCost(StateType src, ActionType action) {
		float result = 0;
		for (StateType dst : apply(src, action)) {
			result = Math.max(result, transitionCost(src, action, dst));
		}
		return result;
	}

	/**
	 * Returns a lower bound on the cost of any path from the given state to a goal
	 * state.
	 */
	public default float estimateDistToGoal(StateType state) {
		return 0;
	}
}