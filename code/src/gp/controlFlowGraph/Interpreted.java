package gp.controlFlowGraph;

import java.util.Optional;

import gp.Domain.Guard;
import gp.Domain.Update;
import gp.Domain.Value;

/**
 * An abstract representation of a program.
 * 
 * @author romanm
 */
public interface Interpreted<ValueType extends Value, UpdateType extends Update, GuardType extends Guard> {
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
	public Optional<ValueType> execute(ValueType input, int maxSteps);
}
