package gp.tmti;

import java.util.Optional;

import gp.Domain;
import gp.Domain.Guard;
import gp.Domain.Update;
import gp.Domain.Value;
import gp.Interpreter;
import gp.Plan;

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
		implements Interpreter<ValueType, UpdateType, GuardType> {
	private final Automaton automaton;
	private final Domain<ValueType, UpdateType, GuardType> domain;

	public AutomatonInterpreter(Automaton automaton, Domain<ValueType, UpdateType, GuardType> domain) {
		this.automaton = automaton;
		this.domain = domain;
	}

	@Override
	public Optional<ValueType> run(ValueType input, int maxSteps) {
		return run(input, maxSteps, null);
	}

	@Override
	public Optional<ValueType> run(ValueType input, int maxSteps, Plan<ValueType, UpdateType> trace) {
		if (trace != null) {
			trace.setFirst(input);
		}
		var currState = automaton.getInitial();
		var stepCounter = 0;
		var currValue = input;
		while (currState != automaton.getFinal()) {
			Action matchedAction = null;
			for (var edge : automaton.succEdges(currState)) {
				var action = edge.getLabel();
				@SuppressWarnings("unchecked")
				var guard = (GuardType) action.guard();
				if (domain.test(guard, currValue)) {
					matchedAction = action;
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
				} else {
					return Optional.empty();
				}

			}
			if (stepCounter > maxSteps) {
				return Optional.empty();
			}
		}
		return Optional.of(currValue);
	}
}
