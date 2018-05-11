package gp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import gp.Domain.Guard;
import gp.Domain.Update;
import gp.Domain.Value;
import gp.planning.Planner;
import gp.separation.LinearInferencer;
import gp.tmti.TMTI;

/**
 * Synthesizes a CFG from a list of input-output examples by using the given
 * planner and CFG generalizer (which internally uses and condition inferencer).
 * The synthesizer uses the planner to generate a plan for each example and then
 * passes the set of plans to the CFG generalizer, which then outputs a CFG.
 * 
 * @author romanm
 *
 * @param <ValueType>
 *            The type of program configurations.
 * @param <UpdateType>
 *            The type of program actions.
 * @param <GuardType>
 *            The type of condition in the program.
 */
public class TMTISynthesizer<ValueType extends Value, UpdateType extends Update, GuardType extends Guard> {
	private final Planner<ValueType, UpdateType> planner;
	private final GPDebugger<ValueType, UpdateType, GuardType> debugger;
	private final Logger logger;

	public TMTISynthesizer(Planner<ValueType, UpdateType> planner, Logger logger,
			GPDebugger<ValueType, UpdateType, GuardType> debugger) {
		assert planner != null;
		this.planner = planner;
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
		logger.info("Generating guards...");
		final var guards = problem.domain().generateGuards(plans);
		logger.info("done");
		debugPrintGuards(guards);
		final var separator = new LinearInferencer<ValueType, UpdateType, GuardType>(problem.domain(), guards);

		logger.info("Generalizing " + plans.size() + " plans...");
		var learner = new TMTI<ValueType, UpdateType, GuardType>(problem.domain(), separator, debugger, logger);
		var learningResult = learner.infer(plans);
		logger.info("Learning result = " + learningResult.type);
		return learningResult.success();
	}

	protected void debugPrintGuards(Collection<GuardType> guards) {
		var txt = new StringBuilder();
		txt.append("#guards=" + guards.size());
		txt.append("=============");
//		for (var guard : guards) {
//			txt.append(guard + "\n");
//		}
		debugger.printCodeFile("guards.txt", txt.toString(), "Available guards");
	}
}