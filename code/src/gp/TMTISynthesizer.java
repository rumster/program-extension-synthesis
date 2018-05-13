package gp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import bgu.cs.util.Timer;
import gp.Domain.Guard;
import gp.Domain.Update;
import gp.Domain.Value;
import gp.planning.Planner;
import gp.separation.LinearInferencer;
import gp.tmti.Automaton;
import gp.tmti.AutomatonInterpreter;
import gp.tmti.TMTI;

/**
 * Synthesizes an {@link Automaton} from a list of examples by using the given
 * planner and the TMTI learner (which internally uses and condition
 * inferencer). The synthesizer uses the planner to generate a plan for each
 * example and then passes the list of plans to TMTI.
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
		var exampleToPlan = new LinkedHashMap<Example<ValueType, UpdateType>, Plan<ValueType, UpdateType>>();
		for (Example<ValueType, UpdateType> example : problem.examples) {
			Optional<Plan<ValueType, UpdateType>> optPlan;
			if (example.inputOnly()) {
				if (problem.interpreter().isPresent()) {
					var interpreter = problem.interpreter().get();
					optPlan = interpreter.genTrace(example.step(0).getT1(), 1000);
				} else {
					logger.info("WARNING: No reference program to complete " + example.name + " (skipped)!");
					optPlan = Optional.empty();
				}
			} else {
				optPlan = PlanningUtils.exampleToPlan(problem.domain(), planner, example, logger);
			}

			if (optPlan.isPresent()) {
				var plan = optPlan.get();
				exampleToPlan.put(example, plan);
				debugger.printPlan(plan, example.id);
				logger.info("Found a plan for example " + example.name);
				if (!example.isTest) {
					plans.add(plan);
				}
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
		var learningTime = new Timer();
		learningTime.start();
		var learningResult = learner.infer(plans);
		learningTime.stop();
		logger.info("Automaton learning time: " + learningTime.toSeconds());
		logger.info("Automaton learning result = " + learningResult.type);
		if (learningResult.success()) {
			var inferredAutomaton = learningResult.get();
			var comparisonResult = compareOnTestExamples(exampleToPlan, inferredAutomaton, problem);
			var synthesisResultStr = comparisonResult ? "success" : "failure";
			logger.info("Synthesis result = " + synthesisResultStr);
			return true;
		}
		return false;
	}

	protected boolean compareOnTestExamples(
			Map<Example<ValueType, UpdateType>, Plan<ValueType, UpdateType>> exampleToPlan, Automaton automaton,
			SynthesisProblem<ValueType, UpdateType, GuardType> problem) {
		var message = new StringBuilder();
		var result = true;
		var numOfTests = 0;
		var numOfTestsSucceeded = 0;
		var exampleToCompareResult = new HashMap<Example<ValueType, UpdateType>, Boolean>();
		for (var entry : exampleToPlan.entrySet()) {
			var example = entry.getKey();
			var examplePlan = entry.getValue();
			if (!example.isTest) {
				continue;
			}
			++numOfTests;
			var interpreter = new AutomatonInterpreter<ValueType, UpdateType, GuardType>(automaton, problem.domain());
			var optAutomatonTrace = interpreter.genTrace(example.input(), 1000);
			if (!optAutomatonTrace.isPresent() || !optAutomatonTrace.get().eqDeterministic(examplePlan)) {
				exampleToCompareResult.put(example, Boolean.FALSE);
				message.append("Testing example " + example.name + ": fail" + System.lineSeparator());
				result = false;
			} else {
				message.append("Testing example " + example.name + ": success" + System.lineSeparator());
				exampleToCompareResult.put(example, Boolean.TRUE);
				++numOfTestsSucceeded;
			}
		}
		message.append("Succeeded on " + numOfTestsSucceeded + " out of " + numOfTests + " test examples.");
		debugger.printCodeFile("Synthesizer message", message.toString(), "Synthesis test results");
		return result;
	}

	protected void debugPrintGuards(Collection<GuardType> guards) {
		var txt = new StringBuilder();
		txt.append("#guards=" + guards.size());
		txt.append("=============");
		// for (var guard : guards) {
		// txt.append(guard + "\n");
		// }
		debugger.printCodeFile("guards.txt", txt.toString(), "Available guards");
	}
}