package gp;

/**
 * A synthesis specification.
 * 
 * @author romanm
 *
 * @param <StateType>
 *            The type of states over which the specification is given.
 */
public abstract class SynthesisProblem<StateType, ActionType, ConditionType> {
	public final String name;
	
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
	public SynthesisProblem(String name) {
		this.name = name;
	}
	
	public abstract Domain<StateType, ActionType, ConditionType> domain();

	/**
	 * Tests whether the given control-flow graph satisfies the specification.
	 * 
	 * @param cfg
	 *            A control-flow graph.
	 */
	public abstract boolean holds(CFG<ActionType, ConditionType> cfg);
}