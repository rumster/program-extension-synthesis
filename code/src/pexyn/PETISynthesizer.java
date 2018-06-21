package pexyn;

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
import pexyn.Semantics.Guard;
import pexyn.Semantics.Cmd;
import pexyn.Semantics.ErrorStore;
import pexyn.Semantics.Store;
import pexyn.generalization.Automaton;
import pexyn.generalization.AutomatonInterpreter;
import pexyn.generalization.Result;
import pexyn.guardInference.ConditionInferencer;
import pexyn.guardInference.DTreeInferencer;
import pexyn.guardInference.ID3Inferencer;
import pexyn.guardInference.LinearInferencer;
import pexyn.generalization.PETI;
import pexyn.planning.Planner;

/**
 * Synthesizes an {@link Automaton} from a list of examples by using the given
 * planner and the PETI learner (which internally uses and condition
 * inferencer). The synthesizer uses the planner to generate a plan for each
 * example and then passes the list of plans to PETI.
 * 
 * @author romanm
 *
 * @param <StoreType>
 *            The type of program configurations.
 * @param <CmdType>
 *            The type of program actions.
 * @param <GuardType>
 *            The type of condition in the program.
 */
public class PETISynthesizer<StoreType extends Store, CmdType extends Cmd, GuardType extends Guard> {
	public final int maxTraceLength;

	private final Planner<StoreType, CmdType> planner;
	private final Configuration config;
	private final GPDebugger<StoreType, CmdType, GuardType> debugger;
	private final Logger logger;

	public PETISynthesizer(Planner<StoreType, CmdType> planner, Configuration config, Logger logger,
			GPDebugger<StoreType, CmdType, GuardType> debugger) {
		assert planner != null;
		this.config = config;
		this.planner = planner;
		this.logger = logger;
		this.debugger = debugger;
		maxTraceLength = config.getInt("pexyn.maxTraceLength", 200);
	}

	public Result synthesize(SynthesisProblem<StoreType, CmdType, GuardType> problem) {
		var exampleToPlan = genPlans(problem);
		var trainingPlans = new ArrayList<Trace<StoreType, CmdType>>();
		exampleToPlan.forEach((example, plan) -> {
			if (!example.isTest) {
				trainingPlans.add(plan);
			}
		});

		ConditionInferencer<StoreType, CmdType, GuardType> separator;
		var guardInfAlgName = config.getString("pexyn.guardInferenceAlgorithm", "");
		var shortCiruitEvaluationSemantics = config.getBoolean("pexyn.shortCiruitEvaluationSemantics", true);
		if (guardInfAlgName.equals("ID3")) {
			separator = new ID3Inferencer<StoreType, CmdType, GuardType>(problem.semantics(), trainingPlans);
		} else if (guardInfAlgName.equals("dtree")) {
			var basicGuards = problem.semantics().generateBasicGuards(trainingPlans);
			separator = new DTreeInferencer<StoreType, CmdType, GuardType>(problem.semantics(), basicGuards,
					shortCiruitEvaluationSemantics);
		} else {
			separator = new LinearInferencer<StoreType, CmdType, GuardType>(problem.semantics(), trainingPlans);
		}
		debugPrintGuards(separator.guards());

		logger.info("Generalizing " + trainingPlans.size() + " plans...");
		var learner = new PETI<StoreType, CmdType, GuardType>(problem.semantics(), separator, debugger, logger);
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
	protected Map<Example<StoreType, CmdType>, Trace<StoreType, CmdType>> genPlans(
			SynthesisProblem<StoreType, CmdType, GuardType> problem) {
		var exampleToPlan = new LinkedHashMap<Example<StoreType, CmdType>, Trace<StoreType, CmdType>>();
		for (Example<StoreType, CmdType> example : problem.examples) {
			Optional<Trace<StoreType, CmdType>> optPlan;
			if (example.inputOnly()) {
				if (problem.interpreter().isPresent()) {
					var interpreter = problem.interpreter().get();
					optPlan = interpreter.genTrace(example.step(0).getT1(), maxTraceLength);
				} else {
					logger.info("WARNING: No reference program to complete " + example.name + " (skipped)!");
					optPlan = Optional.empty();
				}
			} else {
				optPlan = PlanningUtils.exampleToPlan(problem.semantics(), planner, example, logger);
			}

			if (optPlan.isPresent()) {
				if (optPlan.get().lastState() instanceof ErrorStore) {
					var errorStore = (ErrorStore) optPlan.get().lastState();
					logger.info("Example " + example.name + " yields an error store (skipped): " + errorStore.message());
					continue;
				} else {
					var plan = optPlan.get();
					exampleToPlan.put(example, plan);
					debugger.printPlan(plan, example.id);
					logger.info("Found a plan for example " + example.name);
				}
			} else {
				logger.info("No plan for example " + example.name);
				continue;
			}
		}
		return exampleToPlan;
	}

	protected boolean compareOnTestExamples(Map<Example<StoreType, CmdType>, Trace<StoreType, CmdType>> exampleToPlan,
			Automaton automaton, SynthesisProblem<StoreType, CmdType, GuardType> problem) {
		var message = new StringBuilder();
		var result = true;
		var numOfTests = 0;
		var numOfTestsSucceeded = 0;
		var exampleToCompareResult = new HashMap<Example<StoreType, CmdType>, Boolean>();
		for (var entry : exampleToPlan.entrySet()) {
			var example = entry.getKey();
			var plan = entry.getValue();
			if (!example.isTest) {
				continue;
			}
			++numOfTests;
			var interpreter = new AutomatonInterpreter<StoreType, CmdType, GuardType>(automaton, problem.semantics());
			var optAutomatonTrace = interpreter.genTrace(example.input(), maxTraceLength);
			if (!optAutomatonTrace.isPresent() || !optAutomatonTrace.get().eqDeterministic(plan)) {
				{
					if (!optAutomatonTrace.isPresent()) {
						debugger.addCodeFile("diff_" + example.name + " .txt", "No trace",
								"Difference on example " + example.name);
					} else {
						var diffAutomaton = PETI.prefixAutomaton(List.of(optAutomatonTrace.get(), plan),
								problem.semantics(), debugger);
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

	protected void visualizeDiff(Trace<StoreType, CmdType> trace1, Trace<StoreType, CmdType> trace2,
			SynthesisProblem<StoreType, CmdType, GuardType> problem, String description) {
		var diffAutomaton = PETI.prefixAutomaton(List.of(trace1, trace2), problem.semantics(), debugger);
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