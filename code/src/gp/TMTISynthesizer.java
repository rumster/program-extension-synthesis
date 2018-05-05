package gp;

import java.util.ArrayList;
import java.util.logging.Logger;

import gp.Domain.Guard;
import gp.Domain.Update;
import gp.Domain.Value;
import gp.planning.Planner;
import gp.separation.ConditionInferencer;
import gp.tmti.TMTI;

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
public class TMTISynthesizer<ValueType extends Value, UpdateType extends Update, GuardType extends Guard> {
	private final Planner<ValueType, UpdateType> planner;
	private final ConditionInferencer<ValueType, GuardType> separator;
	private final GPDebugger<ValueType, UpdateType, GuardType> debugger;
	private final Logger logger;

	public TMTISynthesizer(Planner<ValueType, UpdateType> planner, ConditionInferencer<ValueType, GuardType> separator,
			Logger logger, GPDebugger<ValueType, UpdateType, GuardType> debugger) {
		assert planner != null && separator != null;
		this.planner = planner;
		this.separator = separator;
		this.logger = logger;
		this.debugger = debugger;
	}

	public boolean synthesize(SynthesisProblem<ValueType, UpdateType, GuardType> problem) {
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
		var learner = new TMTI<ValueType, UpdateType, GuardType>(debugger, problem.domain(), separator);
		learner.infer(plans);
		return true;
	}
}