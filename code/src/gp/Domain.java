package gp;

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
public interface Domain<ValueType extends Domain.Value, UpdateType extends Domain.Update, GuardType extends Domain.Guard> {
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
	 */
	public boolean test(GuardType c, ValueType state);
	
	/**
	 * Tests whether the first state matches the second, where the second state may
	 * be a partial state, serving as a (conjunctive) condition for the first state.
	 */
	public boolean match(ValueType first, ValueType second);	

	/**
	 * Attempts to apply the given update to the given value.
	 * 
	 * @param update
	 *            An update.
	 * @param value
	 *            An input value.
	 * @return the resulting value, if the update can be applied to the input value
	 *         and empty otherwise.
	 */
	public abstract Optional<ValueType> apply(UpdateType update, ValueType value);

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