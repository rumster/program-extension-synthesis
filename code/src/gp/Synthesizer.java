package gp;

import java.util.ArrayList;
import java.util.logging.Logger;

import gp.Domain.Guard;
import gp.Domain.Update;
import gp.Domain.Value;
import gp.controlFlowGraph.CFG;
import gp.controlFlowGraph.CFGGeneralizer;
import gp.controlFlowGraph.CFGGeneralizer.Result;
import gp.planning.Planner;

/**
 * Synthesizes a CFG from a list of input-output examples by using the given
 * planner and CFG generalizer (which internally uses and condition inferencer).
 * The synthesizer uses the planner to generate a plan for each example and then
 * passes the set of plans to the CFG generalizer, which then outputs a CFG.
 * 
 * @author romanm
 *
 * @param <Value>
 *            The type of program configurations.
 * @param <Update>
 *            The type of program actions.
 * @param <Guard>
 *            The type of condition in the program.
 */
public class Synthesizer<ValueType extends Value, UpdateType extends Update, GuardType extends Guard> {
	private final Planner<ValueType, UpdateType> planner;

	private final CFGGeneralizer<ValueType, UpdateType, GuardType> cfgGeneralizer;

	private final GPDebugger<ValueType, UpdateType> debugger;

	private final Logger logger;

	public Synthesizer(Planner<ValueType, UpdateType> planner,
			CFGGeneralizer<ValueType, UpdateType, GuardType> cfgGeneralizer, Logger logger,
			GPDebugger<ValueType, UpdateType> debugger) {
		assert planner != null && cfgGeneralizer != null;
		this.planner = planner;
		this.cfgGeneralizer = cfgGeneralizer;
		this.logger = logger;
		this.debugger = debugger;
	}

	public boolean synthesize(SynthesisProblem<ValueType, UpdateType, GuardType> problem,
			CFG<ValueType, UpdateType, GuardType> result) {
		var plans = new ArrayList<Plan<ValueType, UpdateType>>();
		for (Example<ValueType, UpdateType> example : problem.examples) {
			var optPlan = PlanningUtils.exampleToPlan(problem.domain(), planner, example, logger);
			if (optPlan.isPresent()) {
				var plan = optPlan.get();
				debugger.printPlan(plan, example.id);
				logger.info("Found a plan for example " + example.name);
				plans.add(plan);
			} else {
				continue;
			}
		}
		logger.info("Generalizing " + plans.size() + " plans...");
		var generalizationResult = cfgGeneralizer.generalize(plans, result);
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