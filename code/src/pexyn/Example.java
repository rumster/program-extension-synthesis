package pexyn;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import bgu.cs.util.Union2;
import pexyn.Semantics.Cmd;
import pexyn.Semantics.Store;

/**
 * An example used to drive the synthesis algorithm.
 * 
 * @author romanm
 *
 * @param <StoreType>
 *            The type of stores.
 * @param <CmdType>
 *            The type of commands.
 */
public class Example<StoreType extends Store, CmdType extends Cmd> implements Iterable<Union2<StoreType, CmdType>> {
	public final String name;
	public final int id;

	/**
	 * Indicates whether this example is for training (false) or for testing (true)
	 * the result of the synthesis.
	 */
	public boolean isTest = false;

	/**
	 * A list of intermediate steps. The first step is always a state, while the
	 * following steps may be either partial states or actions.
	 */
	protected final List<Union2<StoreType, CmdType>> steps;

	public Example(StoreType input, StoreType goal, int id, String name) {
		assert id >= 0;
		assert name != null && name.length() > 0;
		this.id = id;
		this.name = name;
		this.steps = new ArrayList<>(2);
		steps.add(Union2.ofT1(input));
		steps.add(Union2.ofT1(goal));
	}

	public Example(StoreType input, StoreType goal, int id) {
		this(input, goal, id, "example_" + id);
	}

	public Example(List<Union2<StoreType, CmdType>> steps, int id, String name) {
		assert id >= 0;
		assert name != null && name.length() > 0;
		assert steps != null && !steps.isEmpty();
		this.id = id;
		this.name = name;
		this.steps = steps;
	}

	public Example(List<Union2<StoreType, CmdType>> steps, int id) {
		this(steps, id, "example_" + id);
	}

	public int size() {
		return steps.size();
	}

	/**
	 * Returns the step at the given index, which is either a state of an action.
	 */
	public Union2<StoreType, CmdType> step(int i) {
		return steps.get(i);
	}

	public boolean inputOnly() {
		return steps.size() == 1;
	}

	public StoreType input() {
		return steps.get(0).getT1();
	}

	public StoreType goal() {
		return steps.get(steps.size() - 1).getT1();
	}

	@Override
	public Iterator<Union2<StoreType, CmdType>> iterator() {
		return steps.iterator();
	}

	@Override
	public String toString() {
		return "Example " + name;
	}
}