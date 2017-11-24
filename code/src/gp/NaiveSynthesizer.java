package gp;

/**
 * Synthesizes a CFG from a list of input-output examples by using the given
 * planner and CFG generalizer (which internally uses and condition inferencer).
 * The synthesizer uses the planner to generate a plan for each example and then
 * passes the set of plans to the CFG generalizer, which then outputs a CFG.
 * 
 * @author romanm
 *
 * @param <StateType>
 *            The type of program configurations.
 * @param <ActionType>
 *            The type of program actions.
 * @param <ConditionType>
 *            The type of condition in the program.
 */
public class NaiveSynthesizer<StateType, ActionType, ConditionType> {
	@SuppressWarnings("unused")
	private SynthesisProblem<StateType, ActionType, ConditionType> problem;

	@SuppressWarnings("unused")
	private final Planner<StateType, ActionType> planner;

	@SuppressWarnings("unused")
	private final CFGGeneralizer<StateType, ActionType, ConditionType> cfgGeneralizer;

	public NaiveSynthesizer(Planner<StateType, ActionType> planner,
			CFGGeneralizer<StateType, ActionType, ConditionType> cfgGeneralizer) {
		this.planner = planner;
		this.cfgGeneralizer = cfgGeneralizer;
	}

	public boolean synthesize(SynthesisProblem<StateType, ActionType, ConditionType> problem,
			CFG<ActionType, ConditionType> result) {
		this.problem = problem;
		throw new UnsupportedOperationException("unimplemented!");
	}
}