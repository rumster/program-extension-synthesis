package gp;

/**
 * A domain of problems for which synthesis is needed.
 * 
 * @author romanm
 *
 * @param <StateType>
 *            The typs of states in the domain.
 * @param <ActionType>
 *            The type of basic actions in the domain.
 * @param <ConditionType>
 *            The type of conditions in the domain.
 */
public interface Domain<StateType, ActionType, ConditionType> {
	/**
	 * The name of this domain.
	 */
	public String name();

	/**
	 * Tests whether the given condition holds for the given state.
	 */
	public boolean test(ConditionType c, StateType state);
}