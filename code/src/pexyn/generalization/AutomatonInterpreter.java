package pexyn.generalization;

import java.util.Optional;

import bgu.cs.util.graph.MultiGraph.Edge;
import pexyn.ArrayListPlan;
import pexyn.Domain;
import pexyn.LoadedInterpreter;
import pexyn.Plan;
import pexyn.Domain.Guard;
import pexyn.Domain.Update;
import pexyn.Domain.Value;

/**
 * An interpreter for automata.
 * 
 * @author romanm
 *
 * @param <ValueType>
 *            The type of values in the underlying domain.
 * @param <UpdateType>
 *            The type of updates in the underlying domain.
 * @param <GuardType>
 *            The type of guards in the underlying domain.
 */
public class AutomatonInterpreter<ValueType extends Value, UpdateType extends Update, GuardType extends Guard>
		implements LoadedInterpreter<ValueType, UpdateType, GuardType> {
	private final Automaton automaton;
	private final Domain<ValueType, UpdateType, GuardType> domain;
	private Plan<ValueType, UpdateType> trace;

	public AutomatonInterpreter(Automaton automaton, Domain<ValueType, UpdateType, GuardType> domain) {
		this.automaton = automaton;
		this.domain = domain;
	}

	@Override
	public Optional<ValueType> run(ValueType input, int maxSteps) {
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
				var edgeUpdate = (UpdateType) matchedAction.update;
				var optNextVal = domain.apply(edgeUpdate, currValue);
				if (optNextVal.isPresent()) {
					currValue = optNextVal.get();
					if (trace != null) {
						@SuppressWarnings("unchecked")
						UpdateType update = (UpdateType) matchedAction.update;
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
	public Optional<Plan<ValueType, UpdateType>> genTrace(ValueType input, int maxSteps) {
		trace = new ArrayListPlan<>(input);
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
