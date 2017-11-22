package gp;

import java.util.Collection;

/**
 * An algorithm for inferring conditions that separate two sets of states.
 * 
 * @author romanm
 *
 * @param <StateType>
 *            The type of states.
 * @param <ConditionType>
 *            The type of conditions.
 */
public abstract class ConditionInferencer<StateType, ConditionType> {
	public abstract void inferSeparator(Collection<StateType> first, Collection<StateType> second);
}