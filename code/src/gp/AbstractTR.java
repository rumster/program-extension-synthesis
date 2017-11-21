package gp;

/**
 * Represents an existential abstraction of some concrete transition relation.
 * 
 * @author romanm
 *
 * @param <ConcreteStateType>
 *            The type of states in the concrete transition relation.
 * @param <AbstractStateType>
 *            The type of states in this transition relation.
 * @param <ActionType>
 *            The type of actions.
 */
public interface AbstractTR<ConcreteStateType, AbstractStateType, ActionType>
		extends TR<AbstractStateType, ActionType> {
	/**
	 * Applies abstraction to a concrete state to obtain an abstract state (ideally,
	 * the most precise one) that represents it.
	 * 
	 * @param state
	 *            A concrete state.
	 * @return An abstract state (ideally, the most precise one) that represents the
	 *         given concrete state.
	 */
	public AbstractStateType applyAbstraction(ConcreteStateType state);
}