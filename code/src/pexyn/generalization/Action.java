package pexyn.generalization;

import pexyn.Semantics.Guard;
import pexyn.Semantics.Cmd;

/**
 * An action labeling an automaton transition.
 * 
 * @author romanm
 */
public class Action {
	public final Cmd update;
	private Guard guard;

	public Action(Guard guard, Cmd update) {
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
		// We do not take the guard into account, since it can be externally mutated.
		return update.hashCode() * 31;
	}

	@Override
	public Action clone() {
		var result = new Action(guard, update);
		return result;
	}

	@Override
	public String toString() {
		return guard.toString() + " / " + update.toString();
	}
}