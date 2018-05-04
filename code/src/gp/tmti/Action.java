package gp.tmti;

import gp.Domain.Guard;
import gp.Domain.Update;

/**
 * An action labeling an automaton transition.
 * 
 * @author romanm
 */
public class Action {
	public final Update update;
	private Guard guard;

	public Action(Guard guard, Update update) {
		assert guard != null && update != null;
		this.guard = guard;
		this.update = update;
	}

	public Guard guard() {
		return guard;
	}

	public void setGuarde(Guard guard) {
		this.guard = guard;
	}
}