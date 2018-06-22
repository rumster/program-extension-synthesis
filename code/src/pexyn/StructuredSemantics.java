package pexyn;

import pexyn.Semantics.Cmd;
import pexyn.Semantics.Guard;
import pexyn.Semantics.Store;

/**
 * Defines operations enabling construction of structured programs. That is, a
 * compound command made from sequenceing, conditions, and loop.
 * 
 * @author romanm
 *
 * @param <StoreType>
 *            The type of stores.
 * @param <CmdType>
 *            The type of (compound) commands.
 * @param <GuardType>
 *            The type of guards.
 */
public interface StructuredSemantics<StoreType extends Store, CmdType extends Cmd, GuardType extends Guard>
		extends Semantics<StoreType, CmdType, GuardType> {
	/**
	 * Returns a command that executes 'first' and then 'second.
	 */
	public CmdType sequence(Cmd first, Cmd second);

	public CmdType condition(GuardType cond, Cmd first, Cmd second);

	public CmdType loop(GuardType cond, Cmd body);
}
