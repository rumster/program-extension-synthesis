package gp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import bgu.cs.util.Union2;

/**
 * An example used to drive the synthesis algorithm.
 * 
 * @author romanm
 *
 * @param <StateType>
 *            The type of states.
 */
public class Example<StateType, ActionType> implements Iterable<Union2<StateType, ActionType>> {
	public final String name;
	public final int id;

	/**
	 * A list of intermediate steps. The first step is always a state, while the
	 * following steps may be either partial states or actions.
	 */
	protected final List<Union2<StateType, ActionType>> steps;

	public Example(StateType input, StateType goal, int id, String name) {
		assert id >= 0;
		assert name != null && name.length() > 0;
		this.id = id;
		this.name = name;
		this.steps = new ArrayList<>(2);
		steps.add(Union2.ofT1(input));
		steps.add(Union2.ofT1(goal));
	}

	public Example(StateType input, StateType goal, int id) {
		this(input, goal, id, "example_" + id);
	}

	public Example(List<Union2<StateType, ActionType>> steps, int id, String name) {
		assert id >= 0;
		assert name != null && name.length() > 0;
		assert steps != null && !steps.isEmpty();
		this.id = id;
		this.name = name;
		this.steps = steps;
	}

	public Example(List<Union2<StateType, ActionType>> steps, int id) {
		this(steps, id, "example_" + id);
	}

	public int size() {
		return steps.size();
	}

	/**
	 * Returns the step at the given index, which is either a state of an action.
	 */
	public Union2<StateType, ActionType> step(int i) {
		return steps.get(i);
	}

	public StateType input() {
		return steps.get(0).getT1();
	}

	public StateType goal() {
		return steps.get(steps.size() - 1).getT1();
	}

	@Override
	public Iterator<Union2<StateType, ActionType>> iterator() {
		return steps.iterator();
	}
}