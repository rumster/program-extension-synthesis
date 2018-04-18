package gp;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import gp.controlFlowGraph.CFG;

/**
 * A specification consisting of a list of input-output examples.
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
public abstract class SynthesisProblem<StateType, ActionType, ConditionType> {
	public final String name;

	public static enum SpecTestResult {
		SPEC_FAIL, SPEC_SATISFIED, ILLEGAL;

		@Override
		public String toString() {
			switch (this) {
			case SPEC_FAIL:
				return "fail";
			case SPEC_SATISFIED:
				return "success";
			case ILLEGAL:
				return "program performs an illegal operation";
			}
			return "unexpected test result type!";
		}
	}

	/**
	 * Returns the problem domain for this synthesis problem.
	 */
	public abstract Domain<StateType, ActionType, ConditionType> domain();

	public SynthesisProblem(String name) {
		this.name = name;
	}

	/**
	 * Tests whether the first state matches the second, where the second state may
	 * be a partial state, serving as a (conjunctive) condition for the first state.
	 */
	public abstract boolean match(StateType first, StateType second);

	/**
	 * Applies a deterministic action to a state and returns the result. If the
	 * action is not applicable or there is no single result, an empty result is
	 * returned.
	 */
	public abstract Optional<StateType> apply(ActionType action, StateType state);

	public List<Example<StateType, ActionType>> examples = new ArrayList<>();

	public void addExample(Example<StateType, ActionType> example) {
		this.examples.add(example);
	}

	public void addExample(StateType input, StateType output) {
		this.examples.add(new Example<>(input, output, examples.size()));
	}

	public boolean test(CFG<StateType, ActionType, ConditionType> prog) {
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