package gp;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import gp.Domain.Guard;
import gp.Domain.Update;
import gp.Domain.Value;
import gp.controlFlowGraph.CFG;

/**
 * A specification consisting of a list of input-output examples.
 * 
 * @author romanm
 *
 * @param <Value>
 *            The type of states.
 * @param <Update>
 *            The type of actions.
 * @param <Guard>
 *            The type of conditions.
 */
public abstract class SynthesisProblem<ValueType extends Value, UpdateType extends Update, GuardType extends Guard> {
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
	public abstract Domain<ValueType, UpdateType, GuardType> domain();

	public SynthesisProblem(String name) {
		this.name = name;
	}

	public List<Example<ValueType, UpdateType>> examples = new ArrayList<>();

	public void addExample(Example<ValueType, UpdateType> example) {
		this.examples.add(example);
	}

	public void addExample(ValueType input, ValueType output) {
		this.examples.add(new Example<>(input, output, examples.size()));
	}

	public boolean test(CFG<ValueType, UpdateType, GuardType> prog) {
		for(Example<ValueType, UpdateType> example : this.examples) {
			ValueType input = example.input();
			ValueType goal = example.goal();
			Optional<ValueType> finalState = prog.execute(input, 100000);
			if(!finalState.isPresent() || !goal.equals(finalState.get())) {
				return false;
			}
		}
		return true;
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