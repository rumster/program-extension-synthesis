package pexyn;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import pexyn.Domain.Guard;
import pexyn.Domain.Update;
import pexyn.Domain.Value;

/**
 * A domain for representing systems.
 * 
 * @author romanm
 *
 * @param <ValueType>
 *            The type of data values (configurations) in the domain.
 * @param <UpdateType>
 *            The type of operations that can modify domain values.
 * @param <GuardType>
 *            The type of predicates on domain values.
 */
public interface Domain<ValueType extends Value, UpdateType extends Update, GuardType extends Guard> {
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
	 *            A value of type {@link ValueType}. A cast down should be safe for
	 *            the instantiating domain.
	 */
	public boolean test(GuardType guard, ValueType val);

	/**
	 * Tests whether the first value matches (i.e., subsumed by) the second value.
	 */
	public boolean match(ValueType first, ValueType second);

	/**
	 * Attempts to apply the given update to the given value.
	 * 
	 * @param update
	 *            An update of type {@link UpdateType}.
	 * @param value
	 *            An input value.
	 * @return the resulting value, if the update can be applied to the input value
	 *         and empty otherwise.
	 */
	public abstract Optional<ValueType> apply(UpdateType update, ValueType value);

	/**
	 * Returns a list of likely predicates for the given plans.
	 */
	public List<GuardType> generateGuards(List<Plan<ValueType, UpdateType>> plans);

	/**
	 * Returns a list of likely atomic predicates for the given plans.
	 */
	public List<GuardType> generateBasicGuards(List<Plan<ValueType, UpdateType>> plans);

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
	public default List<GuardType> generateCompleteBasicGuards(List<Plan<ValueType, UpdateType>> plans) {
		var result = new ArrayList<GuardType>();
		var basicGuards = generateBasicGuards(plans);
		result.addAll(basicGuards);
		for (var guard : basicGuards) {
			result.add(not(guard));
		}
		return result;
	}
	
	/**
	 * An interface marking domain values.
	 * 
	 * @author romanm
	 */
	public interface Value {
	}

	/**
	 * An interface marking operations that modify domain values.
	 * 
	 * @author romanm
	 */
	public interface Update {
	}

	/**
	 * An interface for marking predicates over domain values.
	 * 
	 * @author romanm
	 */
	public interface Guard {
	}
}