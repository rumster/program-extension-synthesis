package pexyn.generalization;

/**
 * A class representing the different types of generalization results.
 * 
 * @author romanm
 */
public class Result {
	public ResultType type;
	protected Automaton m;

	public static Result automaton(Automaton m) {
		var result = new Result(ResultType.OK, m);
		return result;
	}

	public static Result failure(ResultType type) {
		var result = new Result(type, null);
		return result;
	}

	public Automaton get() {
		assert type == ResultType.OK;
		return m;
	}

	public boolean success() {
		return type == ResultType.OK;
	}

	protected Result(ResultType type, Automaton m) {
		this.type = type;
		this.m = m;
	}
}