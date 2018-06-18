package guardInference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import bgu.cs.util.rel.Rel2;
import pexyn.Domain.Guard;
import pexyn.Domain.Cmd;
import pexyn.Domain.Store;

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
public abstract class ConditionInferencer<StoreType extends Store, CmdType extends Cmd, GuardType extends Guard> {
	/**
	 * Attempts to infer a predicate that holds for all values in the first
	 * collection and none of the values in the second collection.
	 */
	public abstract Optional<GuardType> infer(Collection<? extends Store> first, Collection<? extends Store> second);

	public abstract List<GuardType> guards();

	public Optional<Map<Cmd, ? extends Guard>> infer(Rel2<Cmd, Store> updateToValue) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Attempts to infer a predicate that holds for all values at a given index of
	 * the given list and none of the values at any other index.
	 */
	public List<Optional<GuardType>> inferList(List<Collection<? extends Store>> setsOfValues) {
		var result = new ArrayList<Optional<GuardType>>(setsOfValues.size());
		for (int i = 0; i < setsOfValues.size(); ++i) {
			var first = setsOfValues.get(i);
			var unionOfAllOtherValues = new ArrayList<Store>();
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