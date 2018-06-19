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
 * @param <StoreType>
 *            The type of stores.
 * @param <CmdType>
 *            The type of domain updates.
 * @param <GuardType>
 *            The type of domain guards.
 */
public abstract class SynthesisProblem<StoreType extends Store, CmdType extends Cmd, GuardType extends Guard> {
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
	public abstract Domain<StoreType, CmdType, GuardType> domain();

	public abstract Optional<LoadedInterpreter<StoreType, CmdType, GuardType>> interpreter();

	public Optional<Trace<StoreType, CmdType>> generate(Example<StoreType, CmdType> inputOnlyExample,
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

	public final List<Example<StoreType, CmdType>> examples = new ArrayList<>();

	public void addExample(Example<StoreType, CmdType> example) {
		this.examples.add(example);
	}

	public void addExample(StoreType input, StoreType output) {
		this.examples.add(new Example<>(input, output, examples.size()));
	}

	public boolean test(LoadedInterpreter<StoreType, CmdType, GuardType> prog) {
		for (Example<StoreType, CmdType> example : this.examples) {
			StoreType input = example.input();
			StoreType goal = example.goal();
			Optional<StoreType> finalState = prog.run(input, 100000);
			if (!finalState.isPresent() || !domain().match(finalState.get(), goal)) {
				return false;
			}
		}
		return true;
	}
}