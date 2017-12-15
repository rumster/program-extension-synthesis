package gp;

import bgu.cs.util.Pair;

public class InputOutputExample<StateType> extends Pair<StateType, StateType> {
	public final String name;
	public final int id;

	public InputOutputExample(StateType first, StateType second, int index, String name) {
		super(first, second);
		this.id = index;
		this.name = name;
	}

	public InputOutputExample(StateType first, StateType second, int index) {
		super(first, second);
		this.id = index;
		this.name = "example_" + index;
	}
}