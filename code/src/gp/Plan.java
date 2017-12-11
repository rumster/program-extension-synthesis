package gp;

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
	 * Returns the number of states in the path.
	 */
	public int size();

	/**
	 * Holds if there are no states in this plan.
	 */
	public default boolean isEmpty() {
		return size() == 0;
	}
	
	public Iterable<StateType> states();
	
	public Iterable<ActionType> actions();

	public StateType stateAt(int i);

	public ActionType actionAt(int i);

	public default StateType first() {
		return stateAt(0);
	}

	public default StateType last() {
		assert !isEmpty();
		return stateAt(size() - 1);
	}

	public void setFirst(StateType state);

	/**
	 * Precondition: <code>!isEmpty()</code>
	 * 
	 * @param action
	 * @param state
	 */
	public void append(ActionType action, StateType state);

	/**
	 * Precondition: <code>!isEmpty()</code>
	 * 
	 * @param action
	 * @param state
	 */
	public void prepend(ActionType action, StateType state);

	/**
	 * Precondition: <code>!isEmpty()</code>
	 * 
	 * @param other
	 */
	public default void appendPlan(Plan<StateType, ActionType> other) {
		if (other.isEmpty()) {
			return;
		} else {
			if (!other.first().equals(this.last())) {
				throw new IllegalArgumentException("First state of given plan must match last state of this plan!");
			}
			for (int i = 0; i < other.size() - 1; ++i) {
				append(other.actionAt(i), other.stateAt(i + 1));
			}
		}
	}

	/**
	 * Precondition: <code>!isEmpty()</code>
	 * 
	 * @param other
	 */
	public void prependPlan(Plan<StateType, ActionType> other);

	// public static String toVerticalString() {
	// if (isEmpty()) {
	// return "[]";
	// }
	// StringBuilder result = new StringBuilder("[");
	// result.append(first());
	// for (int i = 1; i < size(); ++i) {
	// result.append("\n");
	// result.append(actionAt(i - 1));
	// result.append("\n");
	// result.append(stateAt(i));
	// }
	// result.append("]");
	// return result.toString();
	// }
}