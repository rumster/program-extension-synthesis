package gp;

import java.util.Collection;
import java.util.List;

/**
 * An algorithm for inferring program automata from input traces.
 * 
 * @author romanm
 *
 * @param <DataType>
 *            The type of data elements.
 * @param <UpdateType>
 *            The type of data operations.
 * @param <GuardType>
 *            The type of data predicates.
 */
public class TMTI<DataType, UpdateType, GuardType> {
	@SuppressWarnings("unused")
	private final GPDebugger<DataType, UpdateType, GuardType> debugger;

	/**
	 * The possible results of the algorithm.
	 * 
	 * @author romanm
	 */
	public static enum ResultType {
		OK, NON_DETERMINISTIC, OUT_OF_RESOURCES
	};

	public static class Result<DataType, UpdateType, GuardType> {
		public ResultType type;
		protected Automaton<DataType, UpdateType, GuardType> m;

		public static <DataType, UpdateType, GuardType> Result<DataType, UpdateType, GuardType> automaton(
				Automaton<DataType, UpdateType, GuardType> m) {
			var result = new Result<DataType, UpdateType, GuardType>(ResultType.OK, m);
			return result;
		}

		public Automaton<DataType, UpdateType, GuardType> get() {
			assert type == ResultType.OK;
			return m;
		}

		protected Result(ResultType type, Automaton<DataType, UpdateType, GuardType> m) {
			this.type = type;
			this.m = m;
		}
	}

	public TMTI(GPDebugger<DataType, UpdateType, GuardType> debugger) {
		this.debugger = debugger;
	}

	public Result<DataType, UpdateType, GuardType> infer(Collection<Plan<DataType, UpdateType>> traces) {
		Automaton<DataType, UpdateType, GuardType> prefixAutomaton = prefixAutomaton(traces);
		if (!isUpdateDeterministic(prefixAutomaton)) {

		}

		Automaton<DataType, UpdateType, GuardType> result = null;
		return Result.automaton(result);
	}

	protected Automaton<DataType, UpdateType, GuardType> prefixAutomaton(
			Collection<Plan<DataType, UpdateType>> traces) {
		return null;
	}

	protected boolean isUpdateDeterministic(Automaton<DataType, UpdateType, GuardType> m) {
		return false;
	}

	protected class Signature {
		public final int size;
		public List<List<Automaton<DataType, UpdateType, GuardType>.Action>> sign;

		public Signature(int size) {
			this.size = size;
		}
	}
}
