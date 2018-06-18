package pexyn;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import pexyn.Domain.*;

/**
 * A specification consisting of a list of input-output examples.
 * 
 * @author romanm
 *
 * @param <ValueType>
 *            The type of domain values.
 * @param <UpdateType>
 *            The type of domain updates.
 * @param <GuardType>
 *            The type of domain guards.
 */
public abstract class SynthesisProblem<ValueType extends Value, UpdateType extends Update, GuardType extends Guard> {
	public final String name;

	/**
	 * The type of result returned by comparing the result of the synthesizer with a
	 * given specification.
	 * 
	 * @author romanm
	 */
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

	public abstract Optional<LoadedInterpreter<ValueType, UpdateType, GuardType>> interpreter();

	public Optional<Plan<ValueType, UpdateType>> generate(Example<ValueType, UpdateType> inputOnlyExample,
			int maxSteps) {
		var optInterpreter = interpreter();
		if (optInterpreter.isPresent()) {
			var interpreter = optInterpreter.get();
			var result = interpreter.genTrace(inputOnlyExample.input(), maxSteps);
			return result;
		} else {
			return Optional.empty();
		}
	}

	public SynthesisProblem(String name) {
		this.name = name;
	}

	public final List<Example<ValueType, UpdateType>> examples = new ArrayList<>();

	public void addExample(Example<ValueType, UpdateType> example) {
		this.examples.add(example);
	}

	public void addExample(ValueType input, ValueType output) {
		this.examples.add(new Example<>(input, output, examples.size()));
	}

	public boolean test(LoadedInterpreter<ValueType, UpdateType, GuardType> prog) {
		for (Example<ValueType, UpdateType> example : this.examples) {
			ValueType input = example.input();
			ValueType goal = example.goal();
			Optional<ValueType> finalState = prog.run(input, 100000);
			if (!finalState.isPresent() || !domain().match(finalState.get(), goal)) {
				return false;
			}
		}
		return true;
	}
}