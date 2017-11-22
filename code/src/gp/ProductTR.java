package gp;

import java.util.ArrayList;
import java.util.Collection;

import bgu.cs.util.Pair;

/**
 * The product of two transition relations ranging over the same set of actions.
 * 
 * @author romanm
 *
 * @param <StateType1>
 *            The type of states over which the first transition relation is
 *            defined.
 * @param <StateType2>
 *            The type of states over which the second transition relation is
 *            defined.
 * @param <ActionType>
 *            The type of actions over which both transition relations are
 *            defined.
 */
public class ProductTR<StateType1, StateType2, ActionType> implements TR<Pair<StateType1, StateType2>, ActionType> {
	private final TR<StateType1, ActionType> tr1;
	private final TR<StateType2, ActionType> tr2;
	private final CostCombiner costCombiner;

	/**
	 * Combines the costs of two transitions, each from one of the given transition
	 * relations.
	 * 
	 * @author romanm
	 *
	 */
	public static abstract class CostCombiner {
		public abstract float combine(float cost1, float cost2);
	}

	public static final CostCombiner MAX_COMBINER = new CostCombiner() {
		@Override
		public float combine(float cost1, float cost2) {
			return cost1 > cost2 ? cost1 : cost2;
		}
	};

	public ProductTR(TR<StateType1, ActionType> tr1, TR<StateType2, ActionType> tr2) {
		this(tr1, tr2, MAX_COMBINER);
	}

	public ProductTR(TR<StateType1, ActionType> tr1, TR<StateType2, ActionType> tr2, CostCombiner costCombiner) {
		this.tr1 = tr1;
		this.tr2 = tr2;
		this.costCombiner = costCombiner;
	}

	@Override
	public Collection<ActionType> enabledActions(Pair<StateType1, StateType2> state) {
		Collection<ActionType> out1 = tr1.enabledActions(state.first);
		Collection<ActionType> out2 = tr2.enabledActions(state.second);
		out1.retainAll(out2);
		return out1;
	}

	@Override
	public float transitionCost(Pair<StateType1, StateType2> src, ActionType action, Pair<StateType1, StateType2> dst) {
		float cost1 = tr1.transitionCost(src.first, action, dst.first);
		float cost2 = tr2.transitionCost(src.second, action, dst.second);
		return costCombiner.combine(cost1, cost2);
	}

	@Override
	public float actionCost(Pair<StateType1, StateType2> src, ActionType action) {
		float cost1 = tr1.actionCost(src.first, action);
		float cost2 = tr2.actionCost(src.second, action);
		return costCombiner.combine(cost1, cost2);
	}

	@Override
	public Collection<Pair<StateType1, StateType2>> apply(Pair<StateType1, StateType2> state, ActionType action) {
		Collection<Pair<StateType1, StateType2>> result = new ArrayList<>();
		for (ActionType outAction : enabledActions(state)) {
			for (StateType1 dst1 : tr1.apply(state.first, outAction)) {
				for (StateType2 dst2 : tr2.apply(state.second, outAction)) {
					result.add(new Pair<>(dst1, dst2));
				}
			}
		}
		return result;
	}
}