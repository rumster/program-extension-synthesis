package gp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An example used to drive the synthesis.
 * 
 * @author romanm
 *
 * @param <StateType>
 *            The type of states.
 */
public class Example<StateType> implements Iterable<StateType> {
	public final String name;
	public final int id;

	/**
	 * A list of intermediate states. The first state is the input and the last
	 * state is the goal state.
	 */
	protected final List<StateType> stages;

	public Example(StateType input, StateType goal, int id, String name) {
		assert id >= 0;
		assert name != null && name.length() > 0;
		this.id = id;
		this.name = name;
		this.stages = new ArrayList<>(2);
		stages.add(input);
		stages.add(goal);
	}

	public Example(StateType input, StateType goal, int id) {
		this(input, goal, id, "example_" + id);
	}

	public Example(List<StateType> states, int id, String name) {
		assert id >= 0;
		assert name != null && name.length() > 0;
		assert states != null && !states.isEmpty();
		this.id = id;
		this.name = name;
		this.stages = states;
	}

	public Example(List<StateType> states, int id) {
		this(states, id, "example_" + id);
	}

	public StateType input() {
		return stages.get(0);
	}

	public StateType goal() {
		return stages.get(stages.size() - 1);
	}

	@Override
	public Iterator<StateType> iterator() {
		return stages.iterator();
	}
}