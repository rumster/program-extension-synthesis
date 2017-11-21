package gp;

import java.util.Collection;
import java.util.List;

public interface Operator<ActionType> {
	public List<ArgType> signature();

	public ActionType instantiate(List<Arg> arguments);

	public Collection<ActionType> instantiateAll(Collection<Arg> args);
}