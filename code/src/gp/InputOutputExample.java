package gp;

import bgu.cs.util.Pair;

public class InputOutputExample<StateType> extends Pair<StateType, StateType> {
	public final int index;

	public InputOutputExample(StateType first, StateType second, int index) {
		super(first, second);
		this.index = index;
	}
}