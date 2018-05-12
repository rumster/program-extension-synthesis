package gp;

import java.util.Optional;

import gp.Domain.Guard;
import gp.Domain.Update;
import gp.Domain.Value;

/**
 * Interprets a program.
 * 
 * @author romanm
 */
public interface Interpreter<ValueType extends Value, UpdateType extends Update, GuardType extends Guard> {
	/**
	 * Applies the interpreter to the given input state for at most the given number
	 * of steps.
	 * 
	 * @param input
	 *            The input state.
	 * @param maxSteps
	 *            Maximal number of basic statement applications before declaring
	 *            failure.
	 * @return The output value or empty if the maximal number of steps has been
	 *         exceeded.
	 */
	public Optional<ValueType> run(ValueType input, int maxSteps);

	/**
	 * Applies the interpreter to the given input state for at most the given number
	 * of steps.
	 * 
	 * @param input
	 *            The input state.
	 * @param maxSteps
	 *            Maximal number of basic statement applications before declaring
	 *            failure.
	 * @param trace
	 *            The trace resulting from executing the updates for this run.
	 * @return The output value or empty if the maximal number of steps has been
	 *         exceeded.
	 */
	public Optional<ValueType> run(ValueType input, int maxSteps, Plan<ValueType, UpdateType> trace);
}
