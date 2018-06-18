package gp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.apache.commons.configuration2.Configuration;

import bgu.cs.util.Timer;
import gp.Domain.Guard;
import gp.Domain.Update;
import gp.Domain.Value;
import gp.planning.Planner;
import gp.separation.ConditionInferencer;
import gp.separation.DTreeInferencer;
import gp.separation.ID3Inferencer;
import gp.separation.LinearInferencer;
import gp.tmti.Automaton;
import gp.tmti.AutomatonInterpreter;
import gp.tmti.Result;
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
	public final int maxTraceLength;

	private final Planner<ValueType, UpdateType> planner;
	private final Configuration config;
	private final GPDebugger<ValueType, UpdateType, GuardType> debugger;
	private final Logger logger;

	public TMTISynthesizer(Planner<ValueType, UpdateType> planner, Configuration config, Logger logger,
			GPDebugger<ValueType, UpdateType, GuardType> debugger) {
		assert planner != null;
		this.config = config;
		this.planner = planner;
		this.logger = logger;
		this.debugger = debugger;
		maxTraceLength = config.getInt("pexyn.maxTraceLength", 200);
	}

	public Result synthesize(SynthesisProblem<ValueType, UpdateType, GuardType> problem) {
		var exampleToPlan = genPlans(problem);
		var trainingPlans = new ArrayList<Plan<ValueType, UpdateType>>();
		exampleToPlan.forEach((example, plan) -> {
			if (!example.isTest) {
				trainingPlans.add(plan);
			}
		});

		ConditionInferencer<ValueType, UpdateType, GuardType> separator;
		var guardInfAlgName = config.getString("pexyn.guardInferenceAlgorithm", "");
		if (guardInfAlgName.equals("ID3")) {
			separator = new ID3Inferencer<ValueType, UpdateType, GuardType>(problem.domain(), trainingPlans);
		} else if (guardInfAlgName.equals("dtree")) {
			separator = new DTreeInferencer<ValueType, UpdateType, GuardType>(problem.domain(), trainingPlans);
		} else {
			separator = new LinearInferencer<ValueType, UpdateType, GuardType>(problem.domain(), trainingPlans);
		}
		debugPrintGuards(separator.guards());

		logger.info("Generalizing " + trainingPlans.size() + " plans...");
		var learner = new TMTI<ValueType, UpdateType, GuardType>(problem.domain(), separator, debugger, logger);
		var learningTime = new Timer();
		learningTime.start();
		var learningResult = learner.infer(trainingPlans);
		learningTime.stop();
		logger.info("Automaton learning time: " + learningTime.toSeconds());
		logger.info("Automaton learning result = " + learningResult.type);
		if (learningResult.success()) {
			var inferredAutomaton = learningResult.get();
			var comparisonResult = compareOnTestExamples(exampleToPlan, inferredAutomaton, problem);
			var synthesisResultStr = comparisonResult ? "success" : "failure";
			logger.info("Synthesis result = " + synthesisResultStr);
		}
		return learningResult;
	}

	/**
	 * Converts examples to plans.
	 */
	protected Map<Example<ValueType, UpdateType>, Plan<ValueType, UpdateType>> genPlans(
			SynthesisProblem<ValueType, UpdateType, GuardType> problem) {
		var exampleToPlan = new LinkedHashMap<Example<ValueType, UpdateType>, Plan<ValueType, UpdateType>>();
		for (Example<ValueType, UpdateType> example : problem.examples) {
			Optional<Plan<ValueType, UpdateType>> optPlan;
			if (example.inputOnly()) {
				if (problem.interpreter().isPresent()) {
					var interpreter = problem.interpreter().get();
					optPlan = interpreter.genTrace(example.step(0).getT1(), maxTraceLength);
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
			} else {
				logger.info("No plan for example " + example.name);
				continue;
			}
		}
		return exampleToPlan;
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
			var plan = entry.getValue();
			if (!example.isTest) {
				continue;
			}
			++numOfTests;
			var interpreter = new AutomatonInterpreter<ValueType, UpdateType, GuardType>(automaton, problem.domain());
			var optAutomatonTrace = interpreter.genTrace(example.input(), maxTraceLength);
			if (!optAutomatonTrace.isPresent() || !optAutomatonTrace.get().eqDeterministic(plan)) {
				{
					if (!optAutomatonTrace.isPresent()) {
						debugger.addCodeFile("diff_" + example.name + " .txt", "No trace",
								"Difference on example " + example.name);
					} else {
						var diffAutomaton = TMTI.prefixAutomaton(List.of(optAutomatonTrace.get(), plan),
								problem.domain(), debugger);
						debugger.printAutomaton(diffAutomaton.get(), "Difference on example " + example.name);
					}
				}
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
		debugger.addCodeFile("Synthesizer message", message.toString(), "Synthesis test results");
		return result;
	}

	protected void visualizeDiff(Plan<ValueType, UpdateType> trace1, Plan<ValueType, UpdateType> trace2,
			SynthesisProblem<ValueType, UpdateType, GuardType> problem, String description) {
		var diffAutomaton = TMTI.prefixAutomaton(List.of(trace1, trace2), problem.domain(), debugger);
		debugger.printAutomaton(diffAutomaton.get(), "Difference on example " + description);
	}

	protected void debugPrintGuards(Collection<GuardType> guards) {
		final var maxGuardPrintCount = config.getInt("pexyn.printGuardCountBound", -1);
		var txt = new StringBuilder();
		txt.append("#guards=" + guards.size());
		txt.append("\n=============\n");
		var guardCounter = 0;
		for (var guard : guards) {
			txt.append(guard + "\n");
			++guardCounter;
			if (maxGuardPrintCount >= 0 && guardCounter > maxGuardPrintCount) {
				txt.append("...");
				break;
			}
		}
		debugger.addCodeFile("guards.txt", txt.toString(), "Available guards");
	}
}