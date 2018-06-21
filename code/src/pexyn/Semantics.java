package pexyn;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import pexyn.Semantics.Guard;
import pexyn.Semantics.Cmd;
import pexyn.Semantics.Store;

/**
 * A program semantics.
 * 
 * @author romanm
 *
 * @param <StoreType>
 *            The type of data values (configurations) in the semantics.
 * @param <CmdType>
 *            The type of operations that can modify stores.
 * @param <GuardType>
 *            The type of predicates on stores.
 */
public interface Semantics<StoreType extends Store, CmdType extends Cmd, GuardType extends Guard> {
	/**
	 * The name of this semantics.
	 */
	public String name();

	/**
	 * Returns the always-true predicate.
	 */
	public GuardType getTrue();

	/**
	 * Tests whether the given predicate holds for the given store.
	 * 
	 * @param guard
	 *            A value of type {@link GuardType}. A case down should be safe for
	 *            the instantiating semantics.
	 * @param store
	 *            A value of type {@link StoreType}. A cast down should be safe for
	 *            the instantiating semantics.
	 */
	public boolean test(GuardType guard, StoreType store);

	/**
	 * Returns the cost of the given guard, which is used as a preference for guard
	 * inference.
	 */
	public float guardCost(GuardType guard);

	/**
	 * Tests whether the first store matches (i.e., subsumed by) the second store.
	 */
	public boolean match(StoreType first, StoreType second);

	/**
	 * Attempts to apply the given update to the given value.
	 * 
	 * @param update
	 *            An update of type {@link CmdType}.
	 * @param value
	 *            An input value.
	 * @return the resulting value, if the update can be applied to the input value
	 *         and empty otherwise.
	 */
	public abstract Optional<StoreType> apply(CmdType update, StoreType value);

	/**
	 * Returns a list of likely predicates for the given plans.
	 */
	public List<GuardType> generateGuards(List<Trace<StoreType, CmdType>> plans);

	/**
	 * Returns a list of likely atomic predicates for the given plans.
	 */
	public List<GuardType> generateBasicGuards(List<Trace<StoreType, CmdType>> plans);

	/**
	 * Constructs a disjunction of guards.
	 */
	public GuardType or(GuardType l, GuardType r);

	/**
	 * Constructs a conjunction of guards.
	 */
	public GuardType and(GuardType l, GuardType r);

	/**
	 * Constructs a negated guard.
	 */
	public GuardType not(GuardType l);

	/**
	 * An optional operation. Returns a command that executes 'first' and then
	 * 'second.
	 */
	public CmdType sequence(Cmd first, Cmd second);

	/**
	 * Returns a complete list (including Boolean negation) of likely atomic
	 * predicates for the given plans
	 */
	public default List<GuardType> generateCompleteBasicGuards(List<Trace<StoreType, CmdType>> plans) {
		var result = new ArrayList<GuardType>();
		var basicGuards = generateBasicGuards(plans);
		result.addAll(basicGuards);
		for (var guard : basicGuards) {
			result.add(not(guard));
		}
		return result;
	}

	/**
	 * An interface marking semantics stores (values).
	 * 
	 * @author romanm
	 */
	public interface Store {
	}

	/**
	 * A marker interface for a store resulting from an application of a command to
	 * a store that is not in its domain.
	 * 
	 * @author romanm
	 */
	public interface ErrorStore {
		/**
		 * Returns a textual description of the error.
		 */
		public String message();
	}

	/**
	 * An interface marking operations that modify semantics stores.
	 * 
	 * @author romanm
	 */
	public interface Cmd {
	}

	/**
	 * An interface for marking predicates over stores.
	 * 
	 * @author romanm
	 */
	public interface Guard {
	}
}