package gp.tmti;

import java.util.Collection;
import java.util.HashSet;

import bgu.cs.util.graph.MultiGraph;
import gp.Domain;
import gp.Domain.Guard;
import gp.Domain.Update;
import gp.Domain.Value;
import gp.GPDebugger;
import gp.Plan;

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
	@SuppressWarnings("unused")
	private final GPDebugger<ValueType, UpdateType> debugger;

	private final Domain<ValueType, UpdateType, GuardType> domain;

	/**
	 * Constructs an instance of the algorithm.
	 * 
	 * @param debugger
	 */
	public TMTI(GPDebugger<ValueType, UpdateType> debugger, Domain<ValueType, UpdateType, GuardType> domain) {
		this.debugger = debugger;
		this.domain = domain;
	}

	/**
	 * Runs the algorithm on the given collection of example traces.
	 */
	public Result infer(Collection<Plan<Value, Update>> traces) {
		Automaton prefixAutomaton = prefixAutomaton(traces);
		if (!isUpdateDeterministic(prefixAutomaton)) {
		}

		Automaton result = null;
		return Result.automaton(result);
	}

	/**
	 * Constructs a prefix automaton out of a collection of traces.
	 */
	protected Automaton prefixAutomaton(Collection<Plan<Value, Update>> traces) {
		var result = new Automaton();
		var currState = result.getInitial();
		for (Plan<Value, Update> trace : traces) {
			for (int i = 0; i < trace.size(); ++i) {
				var point = new TracePoint(trace, i);
				currState.addTracePoint(point);
				Update update = trace.actionAt(i);
				var optTransition = result.findTransition(currState, update);
				if (optTransition.isPresent()) {
					currState = optTransition.get().second;
				} else {
					State nextState;
					if (i == trace.size() - 1) {
						nextState = result.getFinal();
					} else {
						nextState = new State();
					}
					var transitionLabel = new Action(domain.getTrue(), update);
					result.addEdge(currState, nextState, transitionLabel);
					currState = nextState;
				}
			}
		}
		return result;
	}

	public static boolean isUpdateDeterministic(Automaton m) {
		for (State state : m.getNodes()) {
			var stateUpdates = new HashSet<Update>();
			for (MultiGraph.Edge<State, Action> transition : m.succEdges(state)) {
				boolean freshUpdate = stateUpdates.add(transition.getLabel().update);
				if (!freshUpdate) {
					return false;
				}
			}
		}
		return true;
	}

	protected boolean equivalentStates(int k) {
		return false;
	}
}
