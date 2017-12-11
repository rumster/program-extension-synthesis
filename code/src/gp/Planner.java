package gp;

import java.util.function.Predicate;

/**
 * An algorithm that finds a path of actions from the given input state to a
 * state satisfying the goal.
 * 
 * @author romanm
 *
 * @param <StateType>
 *            The type of states in the state space.
 * @param <ActionType>
 *            The type of actions that label transitions in the transition
 *            relation.
 */
public interface Planner<StateType, ActionType> {
	/**
	 * The type of result for a plan search.
	 * 
	 * @author romanm
	 */
	public static enum PlanResultType {
		OK, NO_PLAN_EXISTS, TIMEOUT
	}

	/**
	 * Attempts to finds a sequence of actions from the given input state to a state
	 * satisfying the goal.
	 * 
	 * @param input
	 *            The start state.
	 * @param goalTest
	 *            A predicate that tests whether a state satisfies the goal
	 *            condition.
	 * @param addToPlan
	 *            If a plan is found, it is appended to this one.
	 * @return The result of the search.
	 */
	public PlanResultType findPlan(StateType input, Predicate<StateType> goalTest,
			Plan<StateType, ActionType> addToPlan);
}