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

	public void setGuard(Guard guard) {
		this.guard = guard;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o == this) {
			return true;
		}
		Action other = (Action) o;
		return this.guard.equals(other.guard) && this.update.equals(other.update);
	}

	@Override
	public int hashCode() {
		return update.hashCode() * 31 + guard.hashCode();
	}

	@Override
	public Action clone() {
		var result = new Action(guard, update);
		return result;
	}
	
	@Override
	public String toString() {
		return guard.toString() + " / "+ update.toString();
	}
}