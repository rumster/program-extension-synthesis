package gp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

import gp.controlFlowGraph.CFG;
import gp.controlFlowGraph.CFGGeneralizer;
import gp.controlFlowGraph.CFGGeneralizer.Result;

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
			CFG<StateType, ActionType, ConditionType> result) {
		ArrayList<Plan<StateType, ActionType>> plans = new ArrayList<>();
		for (Example<StateType> example : problem.examples) {
			if (example.stages.size() == 1) {
				continue;
			}
			
			List<Predicate<StateType>> goals = new ArrayList<>(example.stages.size() - 1);
			for (int i = 1; i < example.stages.size(); ++i) {
				StateType goalState = example.stages.get(i);
				goals.add(state -> problem.match(state, goalState));
			}

			StateType input = example.input();
			//StateType output = example.goal();
			Plan<StateType, ActionType> plan = new ArrayListPlan<>();
			logger.info("Planning for example " + example.name + "...");
			//SearchResultType planResult = planner.findPlan(input, state -> problem.match(state, output), plan);
			SearchResultType planResult = findPlan(input, goals, plan, example);
			switch (planResult) {
			case OK:
				plans.add(plan);
				debugger.printPlan(plan, example.id);
				logger.info("Found a plan for example " + example.name);
				break;
			case NO_SOLUTION_EXISTS:
				if (logger != null) {
					logger.info("No plan exists for example " + example.name + "! Skipping example.");
				}
				break;
			case OUT_OF_RESOURCES:
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

	public SearchResultType findPlan(StateType input, List<Predicate<StateType>> goalTests,
			Plan<StateType, ActionType> addToPlan, Example<StateType> example) {
		StateType current = input;
		SearchResultType planResult = null;
		for (Predicate<StateType> goal : goalTests) {
			planResult = planner.findPlan(current, goal, addToPlan);
			switch (planResult) {
			case OK:
				current = addToPlan.lastState();
				break;
			case NO_SOLUTION_EXISTS:
				if (logger != null) {
					logger.info("No plan exists for example " + example.name + "! Skipping example.");
				}
				return planResult;
			case OUT_OF_RESOURCES:
				if (logger != null) {
					logger.info("Timed out on example " + example.name + "! Skipping example.");
				}
				return planResult;
			}
		}
		return planResult;
	}
}