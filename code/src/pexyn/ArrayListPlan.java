package pexyn;

import java.util.ArrayList;
import java.util.Collection;

/**
 * An implementation of a plan by a pair of {@link ArrayList}s.
 * 
 * @author romanm
 *
 * @param <StateType>
 *            The type of states.
 * @param <ActionType>
 *            The type of actions applied to states.
 */
public class ArrayListPlan<StateType, ActionType> implements Plan<StateType, ActionType> {
	public final ArrayList<StateType> states = new ArrayList<>();
	public final ArrayList<ActionType> actions = new ArrayList<>();

	public ArrayListPlan(StateType n) {
		this.states.add(n);
	}

	public ArrayListPlan(Collection<StateType> states, Collection<ActionType> actions) {
		assert states != null && actions != null;
		assert states.size() == actions.size() + 1;
		this.states.addAll(states);
		this.actions.addAll(actions);
	}

	@Override
	public void setFirst(StateType n) {
		assert isEmpty();
		this.states.add(n);
	}

	@Override
	public void append(ActionType action, StateType node) {
		assert !isEmpty();
		this.actions.add(action);
		this.states.add(node);
	}

	@Override
	public boolean isEmpty() {
		return states.isEmpty();
	}

	@Override
	public void appendPlan(Plan<StateType, ActionType> other) {
		boolean first = true;
		for (StateType state: other.states()) {
			if (first && !states.isEmpty()) {
				first = false;
				continue;
			}
			this.states.add(state);
			first = false;
		}
		for (ActionType action: other.actions()) {
			this.actions.add(action);
		}
	}

	@Override
	public StateType stateAt(int i) {
		return states.get(i);
	}

	@Override
	public ActionType actionAt(int i) {
		return actions.get(i);
	}

	@Override
	public int size() {
		return states.size();
	}

	@Override
	public void prepend(ActionType action, StateType state) {
		actions.add(0, action);
		states.add(0, state);
	}

	@Override
	public void prependPlan(Plan<StateType, ActionType> other) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<StateType> states() {
		return states;
	}

	@Override
	public Iterable<ActionType> actions() {
		return actions;
	}

	@Override
	public String toString() {
		return actions.toString();
	}
}