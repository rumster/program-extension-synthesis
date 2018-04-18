package gp;

import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Logger;

import bgu.cs.util.Union2;
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

	public boolean synthesize(SynthesisProblem<StateType, ActionType, ConditionType> problem,
			CFG<StateType, ActionType, ConditionType> result) {
		ArrayList<Plan<StateType, ActionType>> plans = new ArrayList<>();
		for (Example<StateType, ActionType> example : problem.examples) {
			assert example.size() > 0;
			if (example.size() == 1 && example.step(0).isT1()) {
				// An example for a possible input.
				continue;
			}

			StateType current;
			Union2<StateType, ActionType> firstStep = example.step(0);
			if (firstStep.isT1()) {
				current = example.input();
			} else {
				throw new IllegalArgumentException("Encountered an example starting with a statement!");
			}

			logger.info("Planning for example " + example.name + "...");
			Plan<StateType, ActionType> plan = new ArrayListPlan<>();
			for (int i = 1; i < example.steps.size(); ++i) {
				boolean skipExample = false;
				Union2<StateType, ActionType> step = example.steps.get(i);
				if (step.isT1()) {
					StateType stateGoal = step.getT1();
					final StateType finalCurrent = current;
					SearchResultType planResult = planner.findPlan(current, state -> {
						return problem.match(finalCurrent, stateGoal);
					}, plan);
					switch (planResult) {
					case OK:
						current = plan.lastState();
						break;
					case NO_SOLUTION_EXISTS:
						if (logger != null) {
							logger.info("No plan exists for example " + example.name + "! Skipping example.");
						}
						skipExample = true;
						break;
					case OUT_OF_RESOURCES:
						if (logger != null) {
							logger.info("Timed out on example " + example.name + "! Skipping example.");
						}
						skipExample = true;
						break;
					}
				} else {
					ActionType action = step.getT2();
					Optional<StateType> next = problem.apply(action, current);
					if (next.isPresent()) {
						current = next.get();
						plan.append(action, current);
					} else {
						if (logger != null) {
							logger.info("Action " + action.toString() + " of example " + example.name + " at step " + i
									+ " is not applicable for the respective state! Skipping example.");
						}
						skipExample = true;
					}
				}
				if (skipExample) {
					continue;
				}
			}

			debugger.printPlan(plan, example.id);
			logger.info("Found a plan for example " + example.name);
			plans.add(plan);
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