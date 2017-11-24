package gp;

import java.util.ArrayList;
import java.util.List;

/**
 * A specification consisting of a list of input-to-list of goals
 * specifications.
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
public class InputGoalSpec<StateType, ActionType, ConditionType> implements Spec<StateType, ActionType, ConditionType> {
	public final List<InputGoals> examples = new ArrayList<>();

	/**
	 * An example in terms of an input state and a list of intermediate goals. The
	 * last goal must hold for the output state.
	 * 
	 * @author romanm
	 */
	public class InputGoals {
		public final StateType input;
		public final List<ConditionType> goals;

		public InputGoals(StateType input, List<ConditionType> goals) {
			this.input = input;
			this.goals = goals;
		}
	}

	@Override
	public boolean holds(CFG<ActionType, ConditionType> cfg) {
		throw new UnsupportedOperationException("unimplemented!");
	}
}