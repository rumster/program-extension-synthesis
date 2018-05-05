package gp.controlFlowGraph;

import java.util.Optional;

/**
 * An abstract representation of a program.
 * 
 * @author romanm
 */
public interface Interpreted <StateType, ActionType>{
	/**
	 * Applies an interpreter to the given input state for at most the given number
	 * of basic statement applications.
	 * 
	 * @param input
	 *            The input state.
	 * @param maxSteps
	 *            Maximal number of basic statement applications before declaring
	 *            failure.
	 * @return The output state or an error state if an error has been encountered
	 *         or the maximal number of steps has passed.
	 */
	public Optional<StateType> execute(StateType input, int maxSteps);
}
