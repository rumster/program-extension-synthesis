package gp;

import java.util.ArrayList;
import java.util.List;

import gp.Domain.Guard;
import gp.Domain.Update;
import gp.Domain.Value;

public interface BooleanDomain<ValueType extends Value, UpdateType extends Update, GuardType extends Guard>
		extends Domain<ValueType, UpdateType, GuardType> {
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
	public default List<GuardType> generateCompleteBasicGuards(ArrayList<Plan<ValueType, UpdateType>> plans) {
		var result = new ArrayList<GuardType>();
		var basicGuards = generateBasicGuards(plans);
		result.addAll(basicGuards);
		for (var guard : basicGuards) {
			result.add(not(guard));
		}
		return result;
	}
}
