package gp;

import java.util.ArrayList;
import java.util.List;

/**
 * A synthesis specification.
 * 
 * @author romanm
 *
 * @param <StateType>
 *            The type of states over which the specification is given.
 */
public class SynthesisProblem<StateType, ActionType, ConditionType> {
	public String name;
	public List<InputOutputExample<StateType>> examples = new ArrayList<>();

	/**
	 * Constructs a named synthesis problem from a problem domain and a list of
	 * examples.
	 * 
	 * @param name
	 *            The name of the problem, typically used for the generated program.
	 * @param domain
	 *            A problem domain.
	 * @param examples
	 *            A set of examples.
	 */
	public SynthesisProblem(String name, ProblemDomain<StateType, ActionType, ConditionType> domain,
			List<InputOutputExample<StateType>> examples) {
		this.name = name;
	}
}