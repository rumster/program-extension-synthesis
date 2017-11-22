package gp;

import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * A deterministic transition relation.
 * 
 * @author romanm
 *
 * @param <StateType>
 *            The type of states in the state space.
 * @param <ActionType>
 *            The type of actions in the transition relation.
 */
public interface DTR<StateType, ActionType> extends TR<StateType, ActionType> {
	/**
	 * Returns the only enabled action on the given state, if one exists, and empty
	 * otherwise.
	 * 
	 * @param state
	 *            A state.
	 */
	public Optional<ActionType> enabledAction(StateType state);

	/**
	 * Applies the currently enabled action, if it exists, to the given state.
	 * 
	 * @param state
	 *            A state.
	 * @return The result of applying the currently enabled actions, if it exists,
	 *         to the given state, and empty otherwise.
	 */
	public Optional<StateType> next(StateType state);

	/**
	 * Executes this transition relation on the given initial state
	 * 
	 * @param initial
	 *            The state from which execution starts.
	 * @param plan
	 *            The plan to which the trace is added.
	 * @param stopper
	 *            A condition for stopping the execution, typically, when the trace
	 *            becomes too long.
	 * @return true if the execution reached a final state and false otherwise.
	 */
	public default boolean trace(StateType initial, Plan<StateType, ActionType> plan,
			BiPredicate<StateType, ActionType> stopper) {
		if (plan.isEmpty()) {
			plan.setFirst(initial);
		} else {
			assert plan.last().equals(initial);
		}

		StateType current = initial;
		while (true) {
			Optional<ActionType> actionOrEmpty = enabledAction(current);
			if (actionOrEmpty.isPresent()) {
				ActionType action = actionOrEmpty.get();
				current = next(current).get();
				plan.append(action, current);
				if (stopper.test(current, action)) {
					return false;
				}

			} else {
				break;
			}
		}
		return true;
	}
}