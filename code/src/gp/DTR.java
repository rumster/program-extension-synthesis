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
	 */
	public Optional<ActionType> enabledAction(StateType state);

	/**
	 * Applies the currently enabled action, if it exists, to the given state.
	 * 
	 * @param state
	 *            A state.
	 * @param plan
	 *            empty plan.
	 * @param timeoutTester
	 *            A predicate for testing whether the execution is taking too many
	 *            steps and should therefore be stopped.
	 * @return The result of applying the currently enabled actions, if it exists,
	 *         to the given state, and empty otherwise.
	 */
	public Optional<StateType> next(StateType state);

	public default boolean trace(StateType initial, Plan<StateType, ActionType> plan,
			BiPredicate<StateType, ActionType> timeoutTester) {
		plan.setFirst(initial);
		StateType current = initial;
		while (true) {
			Optional<ActionType> actionOrEmpty = enabledAction(current);
			if (actionOrEmpty.isPresent()) {
				ActionType action = actionOrEmpty.get();
				current = next(current).get();
				plan.append(action, current);
				if (!timeoutTester.test(current, action)) {
					return false;
				}

			} else {
				break;
			}
		}
		return true;
	}
}