package gp;

/**
 * An example in terms of an input state and a corresponding output state.
 * 
 * @author romanm
 *
 * @param <StateType>
 *            The type of states over which the example is given.
 */
public class InputOutputExample<StateType> {
	public final StateType input;
	public final StateType output;

	public InputOutputExample(StateType input, StateType output) {
		this.input = input;
		this.output = output;
	}
}