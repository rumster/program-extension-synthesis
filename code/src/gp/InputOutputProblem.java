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
public class InputOutputProblem<StateType, ActionType, ConditionType> {
	public final String name;

	public static enum SpecTestResult {
		SPEC_FAIL, SPEC_SATISFIED, ILLEGAL, TOP;

		@Override
		public String toString() {
			switch (this) {
			case SPEC_FAIL:
				return "fail";
			case SPEC_SATISFIED:
				return "success";
			case ILLEGAL:
				return "program performs an illegal operation";
			case TOP:
				return "top";
			}
			return "unexpected test result type!";
		}
	}

	public InputOutputProblem(String name) {
		this.name = name;
	}

	public List<InputOutputExample<StateType>> examples = new ArrayList<>();

	public void addExample(StateType input, StateType output) {
		this.examples.add(new InputOutputExample<>(input, output, examples.size()));
	}

	public boolean test(CFG<ActionType, ConditionType> prog) {
		throw new UnsupportedOperationException("unimplemented!");
		// for (Pair<State, State> example : problem.examples) {
		// State input = example.first;
		// State expectedOutput = example.second;
		// expectedOutput = expectedOutput.clean(problem.getDeadOutVars());
		// State progOutput = prog.execute(input, 100000);
		// progOutput = progOutput.clean(problem.getDeadOutVars());
		// if (progOutput instanceof ErrorState)
		// return SpecTestResult.ILLEGAL;
		// if (progOutput instanceof TopState)
		// return SpecTestResult.TOP;
		// if (!progOutput.equals(expectedOutput))
		// return SpecTestResult.SPEC_FAIL;
		// // Yey, the program satisfies this example.
		// }
		// // Yey, the program satisfies all examples.
		// return SpecTestResult.SPEC_SATISFIED;
	}
}