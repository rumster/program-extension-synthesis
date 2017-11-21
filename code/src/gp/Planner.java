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
	 * Attempts to finds a sequence of actions from the given input state to a state
	 * satisfying the goal.
	 * 
	 * @param input
	 *            The start state.
	 * @param goalTest
	 *            A predicate that tests whether a state satisfies the goal
	 *            condition.
	 * @param addToPath
	 *            If a path is found, it is appended to this one.
	 * @return true if a path exists.
	 */
	public boolean findPlan(StateType input, Predicate<StateType> goalTest, Plan<StateType, ActionType> addToPath);
}