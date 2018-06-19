package pexyn.generalization;

import java.util.Optional;

import bgu.cs.util.graph.MultiGraph.Edge;
import pexyn.ArrayListTrace;
import pexyn.Domain;
import pexyn.LoadedInterpreter;
import pexyn.Trace;
import pexyn.Domain.Guard;
import pexyn.Domain.Cmd;
import pexyn.Domain.Store;

/**
 * An interpreter for automata.
 * 
 * @author romanm
 *
 * @param <StoreType>
 *            The type of values in the underlying domain.
 * @param <CmdType>
 *            The type of updates in the underlying domain.
 * @param <GuardType>
 *            The type of guards in the underlying domain.
 */
public class AutomatonInterpreter<StoreType extends Store, CmdType extends Cmd, GuardType extends Guard>
		implements LoadedInterpreter<StoreType, CmdType, GuardType> {
	private final Automaton automaton;
	private final Domain<StoreType, CmdType, GuardType> domain;
	private Trace<StoreType, CmdType> trace;

	public AutomatonInterpreter(Automaton automaton, Domain<StoreType, CmdType, GuardType> domain) {
		this.automaton = automaton;
		this.domain = domain;
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
				if (domain.test(guard, currValue)) {
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
				var optNextVal = domain.apply(edgeUpdate, currValue);
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
