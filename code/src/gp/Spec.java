package gp;

/**
 * A synthesis specification.
 * 
 * @author romanm
 *
 * @param <StateType>
 *            The type of program configurations.
 * @param <ActionType>
 *            The type of basic actions.
 * @param <ConditionType>
 *            The type of conditions.
 */
public interface Spec<StateType, ActionType, ConditionType> {
	/**
	 * Tests whether the given control-flow graph satisfies the specification.
	 * 
	 * @param cfg
	 *            A control-flow graph.
	 */
	public boolean holds(CFG<ActionType, ConditionType> cfg);
}