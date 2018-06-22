package pexyn.guardInference;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import bgu.cs.util.rel.Rel2;
import pexyn.Semantics.Cmd;
import pexyn.Semantics.Guard;
import pexyn.Semantics.Store;

/**
 * An algorithm for inferring predicates that separate sets of states.
 * 
 * @author romanm
 *
 * @param <StoreType>
 *            The type of states.
 * @param <GuardType>
 *            The type of predicates.
 */
public interface ConditionInferencer<StoreType extends Store, CmdType extends Cmd, GuardType extends Guard> {
	/**
	 * Takes a relation between commands and stores and returns a guard for each
	 * command that holds for all of the stores associated with it and is mutually
	 * exclusive with the guards for the other commands.
	 * 
	 * @param cmdToStore
	 *            A relation between commands and stores.
	 * @return A classifier or empty if the relation is non-deterministic.
	 */
	public Optional<Map<Cmd, ? extends Guard>> infer(Rel2<Cmd, Store> cmdToStore);

	/**
	 * The list of basic guards that are being considered by the inferencer.
	 * 
	 * @return
	 */
	public List<GuardType> guards();
}