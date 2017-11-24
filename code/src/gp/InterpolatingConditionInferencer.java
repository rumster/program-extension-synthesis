package gp;

import java.util.Collection;
import java.util.Iterator;

/**
 * An inferencer based on finding a Boolean Craig interpolant.
 * 
 * @author romanm
 *
 * @param <StateType>
 *            The type of states over which conditions range.
 * @param <ConditionType>
 *            A Boolean condition with an iterator over the basic conditions,
 *            preferably in increasing complexity order.
 */
public class InterpolatingConditionInferencer<StateType, ConditionType extends BooleanCondition & Iterator<ConditionType>>
		extends ConditionInferencer<StateType, ConditionType> {

	@Override
	public void inferSeparator(Collection<StateType> first, Collection<StateType> second) {
		throw new UnsupportedOperationException("unimplemented!");
	}
}