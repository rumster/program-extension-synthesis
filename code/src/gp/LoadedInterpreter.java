package gp;

import java.util.Optional;

import gp.Domain.Guard;
import gp.Domain.Update;
import gp.Domain.Value;

/**
 * An interpreter for a loaded program.
 * 
 * @param <ValueType>
 *            The type of domain values.
 * @param <UpdateType>
 *            The type of domain updates.
 * @param <GuardType>
 *            The type of domain guards.
 * 
 * @author romanm
 */
public interface LoadedInterpreter<ValueType extends Value, UpdateType extends Update, GuardType extends Guard> {
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
	 * Applies the interpreter to the given input state and returns the resulting
	 * trace.
	 * 
	 * @param input
	 *            The input state.
	 * @param maxSteps
	 *            Maximal number of basic statement applications before declaring
	 *            failure.
	 * @return The resulting trace or empty if the maximal number of steps has been
	 *         exceeded.
	 */
	public Optional<Plan<ValueType, UpdateType>> genTrace(ValueType input, int maxSteps);
}
