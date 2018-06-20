package pexyn.generalization;

import java.util.Optional;

import bgu.cs.util.graph.MultiGraph.Edge;
import pexyn.ArrayListTrace;
import pexyn.Semantics;
import pexyn.LoadedInterpreter;
import pexyn.Trace;
import pexyn.Semantics.Guard;
import pexyn.Semantics.Cmd;
import pexyn.Semantics.Store;

/**
 * An interpreter for automata.
 * 
 * @author romanm
 *
 * @param <StoreType>
 *            The type of stores in the underlying semantics.
 * @param <CmdType>
 *            The type of updates in the underlying semantics.
 * @param <GuardType>
 *            The type of guards in the underlying semantics.
 */
public class AutomatonInterpreter<StoreType extends Store, CmdType extends Cmd, GuardType extends Guard>
		implements LoadedInterpreter<StoreType, CmdType, GuardType> {
	private final Automaton automaton;
	private final Semantics<StoreType, CmdType, GuardType> semantics;
	private Trace<StoreType, CmdType> trace;

	public AutomatonInterpreter(Automaton automaton, Semantics<StoreType, CmdType, GuardType> semantics) {
		this.automaton = automaton;
		this.semantics = semantics;
	}

	@Override
	public Optional<StoreType> run(StoreType input, int maxSteps) {
		var currState = automaton.getInitial();
		var stepCounter = 0;
		var currValue = input;
		while (currState != automaton.getFinal()) {
			Action matchedAction = null;
			Edge<State, Action> matchedEdge = null;
			for (var edge : automaton.succEdges(currState)) {
				var action = edge.getLabel();
				@SuppressWarnings("unchecked")
				var guard = (GuardType) action.guard();
				if (semantics.test(guard, currValue)) {
					matchedAction = action;
					matchedEdge = edge;
					break;
				}
			}
			if (matchedAction == null) {
				return Optional.empty();
			} else {
				@SuppressWarnings("unchecked")
				var edgeUpdate = (CmdType) matchedAction.update;
				var optNextVal = semantics.apply(edgeUpdate, currValue);
				if (optNextVal.isPresent()) {
					currValue = optNextVal.get();
					if (trace != null) {
						@SuppressWarnings("unchecked")
						CmdType update = (CmdType) matchedAction.update;
						trace.append(update, currValue);
					}
					++stepCounter;
					if (stepCounter > maxSteps) {
						return Optional.empty();
					}
					currState = matchedEdge.getDst();
				} else {
					return Optional.empty();
				}

			}
		}
		return Optional.of(currValue);
	}

	@Override
	public Optional<Trace<StoreType, CmdType>> genTrace(StoreType input, int maxSteps) {
		trace = new ArrayListTrace<>(input);
		var optVal = run(input, maxSteps);
		if (optVal.isPresent()) {
			var result = trace;
			trace = null; // Release unneeded memory resources.
			return Optional.of(result);
		} else {
			return Optional.empty();
		}
	}
}
