package gp;

/**
 * A synthesis specification.
 * 
 * @author romanm
 *
 * @param <StateType>
 *            The type of states over which the specification is given.
 */
public class SynthesisProblem<StateType, ActionType, ConditionType> {
	public final String name;
	public final Spec<StateType, ActionType, ConditionType> spec;

	/**
	 * Constructs a named synthesis problem from a problem domain and a list of
	 * examples.
	 * 
	 * @param name
	 *            The name of the problem, typically used for the generated program.
	 * @param domain
	 *            A problem domain.
	 * @param spec
	 *            A specification.
	 */
	public SynthesisProblem(String name, ProblemDomain<StateType, ActionType, ConditionType> domain,
			Spec<StateType, ActionType, ConditionType> spec) {
		this.name = name;
		this.spec = spec;
	}
}