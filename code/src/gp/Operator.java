package gp;

import java.util.List;

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
}