package gp.separation;

import java.util.Collection;
import java.util.List;

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
	public abstract List<ConditionType> inferSeparators(List<Collection<StateType>> labelToStates);
}