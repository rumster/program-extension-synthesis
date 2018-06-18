package pexyn;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import pexyn.Domain.Guard;
import pexyn.Domain.Cmd;
import pexyn.Domain.Store;

/**
 * A domain for representing systems.
 * 
 * @author romanm
 *
 * @param <StoreType>
 *            The type of data values (configurations) in the domain.
 * @param <CmdType>
 *            The type of operations that can modify stores.
 * @param <GuardType>
 *            The type of predicates on stores.
 */
public interface Domain<StoreType extends Store, CmdType extends Cmd, GuardType extends Guard> {
	/**
	 * The name of this domain.
	 */
	public String name();

	/**
	 * Returns the always-true predicate.
	 */
	public GuardType getTrue();

	/**
	 * Tests whether the given predicate holds for the given state.
	 * 
	 * @param guard
	 *            A value of type {@link GuardType}. A case down should be safe for
	 *            the instantiating domain.
	 * @param val
	 *            A value of type {@link StoreType}. A cast down should be safe for
	 *            the instantiating domain.
	 */
	public boolean test(GuardType guard, StoreType val);

	/**
	 * Tests whether the first value matches (i.e., subsumed by) the second value.
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
	public List<GuardType> generateGuards(List<Plan<StoreType, CmdType>> plans);

	/**
	 * Returns a list of likely atomic predicates for the given plans.
	 */
	public List<GuardType> generateBasicGuards(List<Plan<StoreType, CmdType>> plans);

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
	 * Returns a complete list (including Boolean negation) of likely atomic
	 * predicates for the given plans
	 */
	public default List<GuardType> generateCompleteBasicGuards(List<Plan<StoreType, CmdType>> plans) {
		var result = new ArrayList<GuardType>();
		var basicGuards = generateBasicGuards(plans);
		result.addAll(basicGuards);
		for (var guard : basicGuards) {
			result.add(not(guard));
		}
		return result;
	}

	/**
	 * An interface marking domain stores (values).
	 * 
	 * @author romanm
	 */
	public interface Store {
	}

	/**
	 * An interface marking operations that modify domain stores.
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