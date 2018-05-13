package gp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import bgu.cs.util.Union2;
import gp.Domain.Update;
import gp.Domain.Value;

/**
 * An example used to drive the synthesis algorithm.
 * 
 * @author romanm
 *
 * @param <ValueType>
 *            The type of domain values.
 * @param <UpdateType>
 *            The type of domain updates.
 */
public class Example<ValueType extends Value, UpdateType extends Update>
		implements Iterable<Union2<ValueType, UpdateType>> {
	public final String name;
	public final int id;

	/**
	 * A list of intermediate steps. The first step is always a state, while the
	 * following steps may be either partial states or actions.
	 */
	protected final List<Union2<ValueType, UpdateType>> steps;

	public Example(ValueType input, ValueType goal, int id, String name) {
		assert id >= 0;
		assert name != null && name.length() > 0;
		this.id = id;
		this.name = name;
		this.steps = new ArrayList<>(2);
		steps.add(Union2.ofT1(input));
		steps.add(Union2.ofT1(goal));
	}

	public Example(ValueType input, ValueType goal, int id) {
		this(input, goal, id, "example_" + id);
	}

	public Example(List<Union2<ValueType, UpdateType>> steps, int id, String name) {
		assert id >= 0;
		assert name != null && name.length() > 0;
		assert steps != null && !steps.isEmpty();
		this.id = id;
		this.name = name;
		this.steps = steps;
	}

	public Example(List<Union2<ValueType, UpdateType>> steps, int id) {
		this(steps, id, "example_" + id);
	}

	public int size() {
		return steps.size();
	}

	/**
	 * Returns the step at the given index, which is either a state of an action.
	 */
	public Union2<ValueType, UpdateType> step(int i) {
		return steps.get(i);
	}

	public ValueType input() {
		return steps.get(0).getT1();
	}

	public ValueType goal() {
		return steps.get(steps.size() - 1).getT1();
	}

	@Override
	public Iterator<Union2<ValueType, UpdateType>> iterator() {
		return steps.iterator();
	}

	@Override
	public String toString() {
		return "Example []";
	}
}