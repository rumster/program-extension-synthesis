package gp.separation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import gp.Domain;
import gp.Domain.Guard;
import gp.Domain.Update;
import gp.Domain.Value;

/**
 * An algorithm for inferring predicates that separate sets of states.
 * 
 * @author romanm
 *
 * @param <ValueType>
 *            The type of states.
 * @param <GuardType>
 *            The type of predicates.
 */
public abstract class ConditionInferencer<ValueType extends Value, UpdateType extends Update, GuardType extends Guard> {
	/**
	 * The domain comprised of values and predicates.
	 */
	public Domain<ValueType, UpdateType, GuardType> domain;

	public ConditionInferencer(Domain<ValueType, UpdateType, GuardType> domain) {
		this.domain = domain;
	}

	/**
	 * Attempts to infer a predicate that holds for all values in the first
	 * collection and none of the values in the second collection.
	 */
	public abstract Optional<GuardType> infer(Collection<? extends Value> first, Collection<? extends Value> second);

	/**
	 * Attempts to infer a predicate that holds for all values at a given index of
	 * the given list and none of the values at any other index.
	 */
	public List<Optional<GuardType>> inferList(List<Collection<? extends Value>> setsOfValues) {
		var result = new ArrayList<Optional<GuardType>>(setsOfValues.size());
		for (int i = 0; i < setsOfValues.size(); ++i) {
			var first = setsOfValues.get(i);
			var unionOfAllOtherValues = new ArrayList<Value>();
			for (int j = 0; j < setsOfValues.size(); ++j) {
				if (i != j) {
					unionOfAllOtherValues.addAll(setsOfValues.get(j));
				}
			}
			var optGuardAtIndex = infer(first, unionOfAllOtherValues);
			result.add(optGuardAtIndex);
		}
		return result;
	}
}