package pexyn;

import java.util.Optional;

import pexyn.Semantics.Guard;
import pexyn.Semantics.Cmd;
import pexyn.Semantics.Store;

/**
 * An interpreter for a loaded program.
 * 
 * @param <StoreType>
 *            The type of stores.
 * @param <CmdType>
 *            The type of commands.
 * @param <GuardType>
 *            The type of guards.
 * 
 * @author romanm
 */
public interface LoadedInterpreter<StoreType extends Store, CmdType extends Cmd, GuardType extends Guard> {
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
	public Optional<StoreType> run(StoreType input, int maxSteps);

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
	public Optional<Trace<StoreType, CmdType>> genTrace(StoreType input, int maxSteps);
}
