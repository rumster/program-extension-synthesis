package gp;

import java.util.ArrayList;
import java.util.List;

/**
 * A specification consisting of a list of input-output state examples.
 * 
 * @author romanm
 *
 * @param <StateType>
 *            The type of states.
 * @param <ActionType>
 *            The type of actions.
 * @param <ConditionType>
 *            The type of conditions.
 */
public class InputOutputSpec<StateType, ActionType, ConditionType>
		implements Spec<StateType, ActionType, ConditionType> {
	public List<InputOutputExample> examples = new ArrayList<>();

	/**
	 * An example in terms of an input state and a corresponding output state.
	 * 
	 * @author romanm
	 *
	 * @param <StateType>
	 *            The type of states over which the example is given.
	 */
	public class InputOutputExample {
		public final StateType input;
		public final StateType output;

		public InputOutputExample(StateType input, StateType output) {
			this.input = input;
			this.output = output;
		}
	}

	@Override
	public boolean holds(CFG<ActionType, ConditionType> cfg) {
		throw new UnsupportedOperationException("unimplemented!");
	}
}