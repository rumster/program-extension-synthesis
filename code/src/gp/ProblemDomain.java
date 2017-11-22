package gp;

import java.util.Collection;

import bgu.cs.util.rel.HashRel2;
import bgu.cs.util.rel.Rel2;

/**
 * A domain of problems for which synthesis is needed.
 * 
 * @author romanm
 *
 * @param <StateType>
 *            The typs of states in the domain.
 * @param <ActionType>
 *            The type of basic actions in the domain.
 * @param <ConditionType>
 *            The type of conditions in the domain.
 */
public abstract class ProblemDomain<StateType, ActionType, ConditionType> {
	/**
	 * The name of this domain.
	 */
	public abstract String name();

	/**
	 * The basic operators available in this domain.
	 */
	public final Collection<Operator<StateType>> operators;

	/**
	 * The arguments available for operators and conditions in this domain.
	 */
	public final Collection<Arg> args;

	/**
	 * Associates arguments types with arguments.
	 */
	protected Rel2<ArgType, Arg> argTypeToArg = new HashRel2<>();

	/**
	 * Constructs a problem domain from a set of operators and a set of arguments.
	 */
	public ProblemDomain(Collection<Operator<StateType>> operators, Collection<Arg> args) {
		this.operators = operators;
		this.args = args;

		for (Arg arg : args) {
			argTypeToArg.add(arg.type, arg);
		}
	}

	/**
	 * Returns all actions obtained by taking every type-correct substitution of the
	 * arguments into the operator.
	 * 
	 * @param operator
	 *            An operator.
	 * @param args
	 *            The available arguments.
	 */
	public Collection<ActionType> instantiateActions(Operator<ActionType> operator, Collection<Arg> args) {
		throw new UnsupportedOperationException("unimplemented!");
	}

	/**
	 * Tests whether the given condition holds for the given state.
	 */
	public abstract boolean test(ConditionType c, StateType state);
}