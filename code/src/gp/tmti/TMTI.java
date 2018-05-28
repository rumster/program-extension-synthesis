package gp.tmti;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import bgu.cs.util.graph.MultiGraph.Edge;
import gp.Domain;
import gp.Domain.Guard;
import gp.Domain.Update;
import gp.Domain.Value;
import gp.GPDebugger;
import gp.Plan;
import gp.separation.ConditionInferencer;

/**
 * An algorithm for inferring program automata from input traces.
 * 
 * @author romanm
 *
 * @param <ValueType>
 *            The type of domain values.
 * @param <UpdateType>
 *            The type of domain updates.
 * @param <GuardType>
 *            The type of domain guards.
 */
public class TMTI<ValueType extends Value, UpdateType extends Update, GuardType extends Guard> {
	private final GPDebugger<ValueType, UpdateType, GuardType> debugger;

	@SuppressWarnings("unused")
	private final Logger logger;

	private final Domain<ValueType, UpdateType, GuardType> domain;

	private final ConditionInferencer<ValueType, UpdateType, GuardType> separator;

	/**
	 * Constructs an instance of the algorithm.
	 * 
	 * @param debugger
	 */
	public TMTI(Domain<ValueType, UpdateType, GuardType> domain,
			ConditionInferencer<ValueType, UpdateType, GuardType> separator,
			GPDebugger<ValueType, UpdateType, GuardType> debugger, Logger logger) {
		assert debugger != null && domain != null && separator != null;
		this.debugger = debugger;
		this.logger = logger;
		this.domain = domain;
		this.separator = separator;
	}

	/**
	 * Runs the algorithm on the given collection of example traces.
	 */
	public Result infer(Collection<Plan<ValueType, UpdateType>> traces) {
		var optPrefixAutomaton = prefixAutomaton(traces, domain, debugger);
		if (!optPrefixAutomaton.isPresent()) {
			return null;
		}
		var prefixAutomaton = optPrefixAutomaton.get();
		debugger.printAutomaton(prefixAutomaton, "Prefix automaton");
		if (!prefixAutomaton.isUpdateDeterministic()) {
			debugger.printTextFile("TMTI message",
					"Unable to learn an automaton: prefix automaton is non-deterministic!", "Synthesizer message");
			return Result.failure(ResultType.NON_DETERMINISTIC);
		}
		var prefixAutomatonGuardAssignable = assignGuards(prefixAutomaton);
		if (!prefixAutomatonGuardAssignable) {
			debugger.printAutomaton(prefixAutomaton, "Prefix automaton with missing guards");
			debugger.printTextFile("TMTI message",
					"Unable to learn an automaton: cannot infer guards for prefix automaton!", "Synthesizer message");
			return Result.failure(ResultType.NON_DETERMINISTIC);
		} else {
			debugger.printAutomaton(prefixAutomaton, "Deterministic prefix automaton");
		}

		for (int lookaheadLength = 1; lookaheadLength < 3; ++lookaheadLength) {
			var optResult = mergeWithLookaheadBound(prefixAutomaton.clone(), lookaheadLength);
			if (optResult.isPresent()) {
				var result = optResult.get();
				return Result.automaton(result);
			}
		}

		return Result.failure(ResultType.OUT_OF_RESOURCES);
	}

	protected Optional<Automaton> mergeWithLookaheadBound(final Automaton automaton, final int lookaheadLength) {
		// Phase 1: Merge only states with maximal signatures.
		var stateToSignature = Signature.getSignatures(automaton, lookaheadLength);
		var maxSignatures = filterMaxSignatures(stateToSignature.all2());
		var change = true;
		while (change) {
			change = false;
			stateToSignature = Signature.getSignatures(automaton, lookaheadLength);
			for (Signature sig : maxSignatures) {
				var equivStates = stateToSignature.select2(sig);
				if (equivStates.size() > 1) {
					change = true;
					var optMergedState = automaton.mergeStates(equivStates);
					debugger.printAutomaton(automaton, "After merging " + equivStates);
					if (optMergedState.isPresent()) {
						var mergedState = optMergedState.get();
						for (var predState : automaton.predStates(mergedState)) {
							automaton.fold(predState);
						}
						automaton.fold(mergedState);
						debugger.printAutomaton(automaton, "After folding " + mergedState);
					}
				}
			}
		}

		// Phase 2: Merge states whose signatures are subsumed by those of other states.
		stateToSignature = Signature.getSignatures(automaton, lookaheadLength);
		// var maxSignaturesPhase2 = filterMaxSignatures(stateToSignature.all2());
		change = true;
		while (change) {
			change = false;
			stateToSignature = Signature.getSignatures(automaton, lookaheadLength);
			for (Signature sig1 : stateToSignature.all2()) {
				for (Signature sig2 : stateToSignature.all2()) {
					if (!sig2.subset(sig1, true)) {
						continue;
					}
					var equivStates1 = stateToSignature.select2(sig1);
					var equivStates2 = stateToSignature.select2(sig2);
					var equivStates = new ArrayList<State>();
					equivStates.addAll(equivStates1);
					equivStates.addAll(equivStates2);
					if (equivStates.size() > 1) {
						change = true;
						var optMergedState = automaton.mergeStates(equivStates);
						debugger.printAutomaton(automaton, "After merging " + equivStates);
						if (optMergedState.isPresent()) {
							var mergedState = optMergedState.get();
							automaton.fold(mergedState);
							debugger.printAutomaton(automaton, "After folding " + mergedState);
						}
					}
				}
			}
		}

		var deterministic = assignGuards(automaton);
		debugger.printAutomaton(automaton,
				"After folding with k=" + lookaheadLength + ":" + (deterministic ? "success" : "failure"));
		return deterministic ? Optional.of(automaton) : Optional.empty();
	}

	protected Collection<Signature> filterMaxSignatures(Collection<Signature> signatures) {
		var result = new ArrayList<Signature>();
		for (var sig1 : signatures) {
			var maximal = true;
			for (var sig2 : signatures) {
				if (sig1.subset(sig2, true)) {
					maximal = false;
					break;
				}
			}
			if (maximal) {
				result.add(sig1);
			}
		}
		return result;
	}

	/**
	 * Attempts to constructs a prefix automaton out of a collection of traces.
	 */
	public static <ValueType extends Value, UpdateType extends Update, GuardType extends Guard> Optional<Automaton> prefixAutomaton(
			Collection<Plan<ValueType, UpdateType>> traces, Domain<ValueType, UpdateType, GuardType> domain,
			GPDebugger<ValueType, UpdateType, GuardType> debugger) {
		var stateCounter = 0;
		var result = new Automaton();
		var traceCounter = 0;
		for (var trace : traces) {
			var currState = result.getInitial();
			for (int i = 0; i < trace.size() - 1; ++i) {
				if (currState == result.getFinal()) {
					debugger.printTextFile("message",
							"Unable to build prefix automaton, since it contains transitions beyond final state with trace "
									+ traceCounter + "!",
							"Synthesizer message");
					return Optional.empty();
				}
				var point = new TracePoint(trace, i);
				currState.addTracePoint(point);
				var update = trace.actionAt(i);
				var optTransition = result.findTransition(currState, update);
				if (optTransition.isPresent()) {
					currState = optTransition.get().second;
				} else {
					State nextState;
					if (i == trace.size() - 2) {
						nextState = result.getFinal();
					} else {
						++stateCounter;
						nextState = new State("N" + stateCounter);
						result.addNode(nextState);
					}
					var transitionLabel = new Action(domain.getTrue(), update);
					result.addEdge(currState, nextState, transitionLabel);
					currState = nextState;
				}
			}
			++traceCounter;
		}
		return Optional.of(result);
	}

	/**
	 * Attempts to assign a guards to every action on a split state.
	 * 
	 * @return If a guard was able to be found for every action on a split state.
	 */
	protected boolean assignGuards(Automaton automaton) {
		for (var state : automaton.getNodes()) {
			if (automaton.outDegree(state) <= 1)
				continue;

			var updates = new ArrayList<Update>();
			List<Collection<? extends Value>> updateValues = new ArrayList<>();
			state.updateToValues().forEach((update, values) -> {
				updates.add(update);
				updateValues.add(values);
			});
			var optGuards = separator.inferList(updateValues);
			var updateToGuard = new HashMap<Update, GuardType>();
			for (int i = 0; i < optGuards.size(); ++i) {
				var optGuard = optGuards.get(i);
				if (optGuard.isPresent()) {
					var guard = optGuard.get();
					var update = updates.get(i);
					updateToGuard.put(update, guard);
				} else {
					logger.info("Failed to assign guards to " + state);
					return false;
				}
			}

			// All guards exist, now set them for each action.
			for (Edge<State, Action> edge : automaton.succEdges(state)) {
				var action = edge.getLabel();
				var guard = updateToGuard.get(action.update);
				action.setGuarde(guard);
			}
		}
		return true;
	}
}
