package gp;

import java.util.ArrayList;
import java.util.Collection;

/**
 * An implementation of a plan by a pair of {@link ArrayList>s.
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

	public ArrayListPlan() {
	}

	public ArrayListPlan(Collection<StateType> states, Collection<ActionType> actions) {
		assert states != null && actions != null;
		assert states.size() == actions.size() + 1;
		this.states.addAll(states);
		this.actions.addAll(actions);
	}

	public void setFirst(StateType n) {
		assert states.isEmpty();
		this.states.add(n);
	}

	public void append(ActionType action, StateType node) {
		this.actions.add(action);
		this.states.add(node);
	}

	public boolean isEmpty() {
		return states.isEmpty();
	}

	public void appendPlan(Plan<StateType, ActionType> other) {
		throw new UnsupportedOperationException();
		/*
		 * if (other.states.size() < 2) return; if (isEmpty()) {
		 * states.addAll(other.states); actions.addAll(other.actions); } else { final
		 * StateType lastState = last(); assert lastState != null; assert
		 * lastState.equals(other.first()); states.addAll(other.states.subList(1,
		 * other.states.size())); actions.addAll(other.actions); }
		 */
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
}