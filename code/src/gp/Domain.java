package gp;

import gp.Domain.Update;
import gp.Domain.Guard;
import gp.Domain.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
	public Guard getTrue();

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
	public List<GuardType> generateGuards(ArrayList<Plan<ValueType, UpdateType>> plans);

	/**
	 * Returns a list of likely atomic predicates for the given plans.
	 */
	public List<GuardType> generateBasicGuards(ArrayList<Plan<ValueType, UpdateType>> plans);
	
	/**
	 * Returns a complete list(including Boolean negation) of likely atomic predicates for the given plans
	 */
	public List<GuardType> generateCompleteBasicGuards(ArrayList<Plan<ValueType, UpdateType>> plans);

	/**
	 * Boolean OR operator
	 */
	public GuardType or(GuardType l, GuardType r);
	
	/**
	 * Boolean AND operator
	 */
	public GuardType and(GuardType l, GuardType r);
	
	/**
	 * Boolean NOT operator
	 */
	public GuardType not(GuardType l);
	
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