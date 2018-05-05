package gp.tmti;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

import bgu.cs.util.graph.MultiGraph;
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
 * @param <Value>
 *            The type of data elements.
 * @param <Update>
 *            The type of data operations.
 * @param <GuardType>
 *            The type of data predicates.
 */
public class TMTI<ValueType extends Value, UpdateType extends Update, GuardType extends Guard> {
	private final GPDebugger<ValueType, UpdateType, GuardType> debugger;

	private final Domain<ValueType, UpdateType, GuardType> domain;

	/**
	 * Constructs an instance of the algorithm.
	 * 
	 * @param debugger
	 */
	public TMTI(GPDebugger<ValueType, UpdateType, GuardType> debugger, Domain<ValueType, UpdateType, GuardType> domain,
			ConditionInferencer<ValueType, GuardType> separator) {
		this.debugger = debugger;
		this.domain = domain;
	}

	/**
	 * Runs the algorithm on the given collection of example traces.
	 */
	public Result infer(Collection<Plan<ValueType, UpdateType>> traces) {
		var optPrefixAutomaton = prefixAutomaton(traces);
		if (!optPrefixAutomaton.isPresent()) {
			return null;
		}
		var prefixAutomaton = optPrefixAutomaton.get();
		debugger.printAutomaton(prefixAutomaton, "Prefix automaton");
		if (!isUpdateDeterministic(prefixAutomaton)) {
			debugger.printTextFile("message", "Unable to learn an automaton: prefix automaton is non-deterministic!",
					"Synthesizer message");
			return Result.failure(ResultType.NON_DETERMINISTIC);
		}
		var prefixAutomatonGuardAssignable = assignGuards(prefixAutomaton);
		if (!prefixAutomatonGuardAssignable) {
			debugger.printTextFile("message", "Unable to learn an automaton: cannot infer guards for prefix automaton!",
					"Synthesizer message");
			return Result.failure(ResultType.NON_DETERMINISTIC);
		}

		Automaton result = null;
		return Result.automaton(result);
	}

	/**
	 * Attempts to constructs a prefix automaton out of a collection of traces.
	 */
	protected Optional<Automaton> prefixAutomaton(Collection<Plan<ValueType, UpdateType>> traces) {
		var stateCounter = 0;
		var result = new Automaton();
		var traceCounter = 0;
		for (var trace : traces) {
			var currState = result.getInitial();
			for (int i = 0; i < trace.size() - 1; ++i) {
				if (currState == result.getFinal()) {
					debugger.printTextFile("message",
							"Unable to learn an automaton: prefix automaton contains transitions beyond final state with trace "
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

	protected boolean assignGuards(Automaton m) {
		for (var state : m.getNodes()) {
			var stateUpdates = new HashSet<Update>();
			for (MultiGraph.Edge<State, Action> transition : m.succEdges(state)) {
			}
		}
		return true;
	}

	/**
	 * Tests whether for each state, the updates that label its outgoing transitions
	 * are all different.
	 */
	public static boolean isUpdateDeterministic(Automaton m) {
		for (var state : m.getNodes()) {
			var stateUpdates = new HashSet<Update>();
			for (MultiGraph.Edge<State, Action> transition : m.succEdges(state)) {
				var freshUpdate = stateUpdates.add(transition.getLabel().update);
				if (!freshUpdate) {
					return false;
				}
			}
		}
		return true;
	}
}
