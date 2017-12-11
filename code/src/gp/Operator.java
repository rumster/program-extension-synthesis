package gp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import bgu.cs.util.CartesianIterator;
import bgu.cs.util.rel.HashRel2;
import bgu.cs.util.rel.Rel2;

/**
 * An operator is a template for actions.
 * 
 * @author romanm
 *
 * @param <ActionType>
 */
public interface Operator<ActionType> {
	/**
	 * The list of parameters needed to instantiate actions from this operator.
	 */
	public List<ArgType> signature();

	/**
	 * Returns the action obtained by substituting the arguments for the operator
	 * parameters.
	 * 
	 * @param arguments
	 *            The actual arguments.
	 */
	public ActionType instantiate(List<Arg> arguments);

	/**
	 * Returns all actions obtained by taking every type-correct substitution of the
	 * arguments into the operator.
	 * 
	 * @param operator
	 *            An operator.
	 * @param args
	 *            The available arguments.
	 */
	public default Collection<ActionType> instantiateActions(Collection<Arg> args) {
		Rel2<ArgType, Arg> argTypeToArg = new HashRel2<>();
		for (Arg arg : args) {
			argTypeToArg.add(arg.type, arg);
		}

		Collection<ActionType> result = new ArrayList<>();
		List<ArgType> signature = signature();
		List<Collection<Arg>> argDomains = new ArrayList<>(signature().size());
		for (int i = 0; i < signature.size(); ++i) {
			argDomains.set(i, argTypeToArg.selectFirst(signature.get(i)));
		}
		ArrayList<Arg> actuals = new ArrayList<>(signature.size());
		CartesianIterator<Arg> argTupleIterator = new CartesianIterator<>(argDomains, actuals);
		while (argTupleIterator.hasNext()) {
			argTupleIterator.next();
			ActionType action = instantiate(actuals);
			result.add(action);
		}
		return result;
	}
}