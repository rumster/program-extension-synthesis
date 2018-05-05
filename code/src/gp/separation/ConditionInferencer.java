package gp.separation;

import java.util.Collection;
import java.util.List;

import gp.Domain;

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
public abstract class ConditionInferencer<StateType extends Domain.Value, ActionType extends Domain.Update, ConditionType extends Domain.Guard> {
	public Domain<StateType, ActionType, ConditionType> domain;
	
	public ConditionInferencer(Domain<StateType, ActionType, ConditionType> domain) {
		this.domain = domain;
	}

	public abstract List<ConditionType> inferSeparators(List<Collection<StateType>> labelToStates);
	
	public abstract ConditionType inferSeparator(Collection<StateType> first, Collection<StateType> second);
}