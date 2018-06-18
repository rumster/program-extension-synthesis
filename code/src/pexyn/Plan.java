package pexyn;

/**
 * Defines a sequence of actions and intermediate states, starting from an input
 * state and leading to a goal state.
 * 
 * @author romanm
 *
 * @param <StateType>
 *            The type of states.
 * @param <ActionType>
 *            The type of actions applied to states.
 */
public interface Plan<StateType, ActionType> {
	/**
	 * Returns the number of states in the plan.
	 */
	public int size();

	/**
	 * Holds if there are no states in this plan.
	 */
	public default boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * Allows iterating over the sequence of states in this trace.
	 */
	public Iterable<StateType> states();

	/**
	 * Allows iterating over the sequence of actions in this trace.
	 */
	public Iterable<ActionType> actions();

	/**
	 * Returns the state at the given position, which must be within 0 and
	 * <code>size()</code>.
	 */
	public StateType stateAt(int i);

	/**
	 * Returns the action at the given position, which must be within 0 and
	 * <code>size()-1</code>.
	 */
	public ActionType actionAt(int i);

	public default StateType firstState() {
		return stateAt(0);
	}

	public default StateType lastState() {
		assert !isEmpty();
		return stateAt(size() - 1);
	}

	public void setFirst(StateType state);

	/**
	 * Precondition: <code>!isEmpty()</code>
	 */
	public void append(ActionType action, StateType state);

	/**
	 * Precondition: <code>!isEmpty()</code>
	 */
	public void prepend(ActionType action, StateType state);

	/**
	 * Precondition: <code>!isEmpty()</code>
	 */
	public default void appendPlan(Plan<StateType, ActionType> other) {
		if (other.isEmpty()) {
			return;
		} else {
			if (!other.firstState().equals(this.lastState())) {
				throw new IllegalArgumentException("First state of given plan must match last state of this plan!");
			}
			for (int i = 0; i < other.size() - 1; ++i) {
				append(other.actionAt(i), other.stateAt(i + 1));
			}
		}
	}

	/**
	 * Precondition: <code>!isEmpty()</code>
	 */
	public void prependPlan(Plan<StateType, ActionType> other);

	/**
	 * Compares two plans, assuming actions are deterministic.
	 */
	public default boolean eqDeterministic(Plan<StateType, ActionType> other) {
		return this.firstState().equals(other.firstState()) && this.actions().equals(other.actions());
	}
}