package pexyn.guardInference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import pexyn.Semantics;
import pexyn.Trace;
import pexyn.Semantics.Guard;
import pexyn.Semantics.Cmd;
import pexyn.Semantics.Store;

/**
 * An inferencer is loosely based on ID3 algorithm.
 * 
 * @author alexanders
 */
public class ID3Inferencer<StoreType extends Store, CmdType extends Cmd, GuardType extends Guard>
		extends ConditionInferencer<StoreType, CmdType, GuardType> {
	private Semantics<StoreType, CmdType, GuardType> domain;
	private final List<GuardType> basicGuards;

	/**
	 * 
	 * @param guards,
	 *            sorted list of basic predicates
	 */
	public ID3Inferencer(Semantics<StoreType, CmdType, GuardType> domain, List<Trace<StoreType, CmdType>> plans) {
		this.domain = domain;
		this.basicGuards = domain.generateCompleteBasicGuards(plans);
	}

	@Override
	public Optional<GuardType> infer(Collection<? extends Store> first, Collection<? extends Store> second) {
		return inferHelper(first, second, basicGuards);
	}

	/*
	 * 1. Choose a basic predicate P1 with minimal number of "mistakes" (negative
	 * qualification on "first" and positive on "second") 2. If the number of
	 * "mistakes" is greater than 0 (separation is not complete): 2.1. Recursively
	 * separate positive first and (false)positive second - P2 2.2. Recursively
	 * separate (false)negative first and negative second - P3 2.3. Combine the
	 * result as (P1 & P2) | (~P1 & P3)
	 */
	private Optional<GuardType> inferHelper(Collection<? extends Store> first, Collection<? extends Store> second,
			List<GuardType> guards) {
		if (guards.isEmpty()) {
			return Optional.empty();
		}

		Collection<? extends Store> bestPositiveFirst = null;
		Collection<? extends Store> bestNegativeFirst = null;
		Collection<? extends Store> bestPositiveSecond = null;
		Collection<? extends Store> bestNegativeSecond = null;
		GuardType bestGuard = null;
		int bestScore = 0;
		for (GuardType guard : guards) {
			@SuppressWarnings("unchecked")
			Collection<? extends Store> positiveFirst = first.stream().filter(e -> domain.test(guard, (StoreType) e))
					.collect(Collectors.toList());
			Collection<? extends Store> negativeFirst = new ArrayList<>(first);
			negativeFirst.removeAll(positiveFirst);

			@SuppressWarnings("unchecked")
			Collection<? extends Store> positiveSecond = second.stream().filter(e -> domain.test(guard, (StoreType) e))
					.collect(Collectors.toList());
			Collection<? extends Store> negativeSecond = new ArrayList<>(second);
			negativeSecond.removeAll(positiveSecond);

			if ((positiveFirst.size() + negativeSecond.size()) > bestScore) {
				bestScore = positiveFirst.size() + negativeSecond.size();
				bestGuard = guard;
				bestPositiveFirst = positiveFirst;
				bestNegativeFirst = negativeFirst;
				bestNegativeSecond = negativeSecond;
				bestPositiveSecond = positiveSecond;
			}

			// complete separation was achieved
			if (bestScore == (first.size() + second.size())) {
				return Optional.of(bestGuard);
			}
		}

		List<GuardType> nextGuards = new ArrayList<>(guards);
		nextGuards.remove(bestGuard);

		Optional<GuardType> left = inferHelper(bestPositiveFirst, bestPositiveSecond, nextGuards);
		if (!left.isPresent()) {
			return Optional.empty();
		}

		Optional<GuardType> right = inferHelper(bestNegativeFirst, bestNegativeSecond, nextGuards);
		if (!right.isPresent()) {
			return Optional.empty();
		}

		return Optional
				.of(domain.or(domain.and(bestGuard, left.get()), domain.and(domain.not(bestGuard), right.get())));
	}

	@Override
	public List<GuardType> guards() {
		return basicGuards;
	}
}
