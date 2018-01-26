package gp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import bgu.cs.util.Union2;

/**
 * An example used to drive the synthesis.
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
	 * A list of intermediate states. The first state is the input and the last
	 * state is the goal state.
	 */
	protected final List<Union2<StateType, ActionType>> stages;

	public Example(StateType input, StateType goal, int id, String name) {
		assert id >= 0;
		assert name != null && name.length() > 0;
		this.id = id;
		this.name = name;
		this.stages = new ArrayList<>(2);
		stages.add(Union2.ofT1(input));
		stages.add(Union2.ofT1(goal));
	}

	public Example(StateType input, StateType goal, int id) {
		this(input, goal, id, "example_" + id);
	}

	public Example(List<Union2<StateType, ActionType>> states, int id, String name) {
		assert id >= 0;
		assert name != null && name.length() > 0;
		assert states != null && !states.isEmpty();
		this.id = id;
		this.name = name;
		this.stages = states;
	}

	public Example(List<Union2<StateType, ActionType>> states, int id) {
		this(states, id, "example_" + id);
	}

	public int size() {
		return stages.size();
	}

	public Union2<StateType, ActionType> step(int i) {
		return stages.get(i);
	}

	public StateType input() {
		return stages.get(0).getT1();
	}

	public StateType goal() {
		return stages.get(stages.size() - 1).getT1();
	}

	@Override
	public Iterator<Union2<StateType, ActionType>> iterator() {
		return stages.iterator();
	}
}