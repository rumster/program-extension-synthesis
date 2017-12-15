package gp;

import java.util.ArrayList;
import java.util.logging.Logger;

import gp.CFGGeneralizer.Result;
import gp.Planner.PlanResultType;

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
	private final Planner<StateType, ActionType> planner;

	private final CFGGeneralizer<StateType, ActionType, ConditionType> cfgGeneralizer;

	private final GPDebugger<StateType, ActionType, ConditionType> debugger;
	private final Logger logger;

	public NaiveSynthesizer(Planner<StateType, ActionType> planner,
			CFGGeneralizer<StateType, ActionType, ConditionType> cfgGeneralizer, Logger logger,
			GPDebugger<StateType, ActionType, ConditionType> debugger) {
		assert planner != null && cfgGeneralizer != null;
		this.planner = planner;
		this.cfgGeneralizer = cfgGeneralizer;
		this.logger = logger;
		this.debugger = debugger;
	}

	public boolean synthesize(InputOutputProblem<StateType, ActionType, ConditionType> problem,
			CFG<ActionType, ConditionType> result) {
		ArrayList<Plan<StateType, ActionType>> plans = new ArrayList<>();
		for (InputOutputExample<StateType> example : problem.examples) {
			StateType input = example.first;
			StateType output = example.second;
			Plan<StateType, ActionType> plan = new ArrayListPlan<>();
			logger.info("Planning for example " + example.name + "...");
			PlanResultType planResult = planner.findPlan(input, state -> problem.match(state, output), plan);
			switch (planResult) {
			case OK:
				plans.add(plan);
				debugger.printPlan(plan, example.id);
				logger.info("Found a plan for example " + example.name);
				break;
			case NO_PLAN_EXISTS:
				if (logger != null) {
					logger.info("No plan exists for example " + example.name + "! Skipping example.");
				}
				break;
			case TIMEOUT:
				if (logger != null) {
					logger.info("Timed out on example " + example.name + "! Skipping example.");
				}
				break;
			}
		}
		logger.info("Generalizing " + plans.size() + " plans...");
		Result generalizationResult = cfgGeneralizer.generalize(plans, result);
		switch (generalizationResult) {
		case OK:
			logger.info("Generalization succeeded!");
			break;
		case CONDITION_INFERENCE_FAILURE:
			logger.info("Generalization was unable to infer conditions!");
			break;
		case OUT_OF_RESOURCES:
			logger.info("Generalization ran out of resources!");
			break;
		}
		return generalizationResult == Result.OK;
	}
}