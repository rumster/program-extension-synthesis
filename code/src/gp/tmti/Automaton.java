package gp.tmti;

import java.util.Optional;

import bgu.cs.util.Pair;
import bgu.cs.util.graph.HashMultiGraph;
import gp.Domain.Update;

/**
 * A program automaton.
 * 
 * @author romanm
 */
public class Automaton extends HashMultiGraph<State, Action> {
	/**
	 * The initial state.
	 */
	private State entry;

	/**
	 * The final state.
	 */
	private State exit;

	public Automaton() {
		entry = new State();
		exit = new State();
		addNode(entry);
		addNode(exit);
	}

	public State getInitial() {
		return entry;
	}

	public State getFinal() {
		return exit;
	}

	public Optional<Pair<Action, State>> findTransition(State src, Update update) {
		Action foundAction = null;
		State foundState = null;
		for (Edge<State, Action> transition : succEdges(src)) {
			Action transitionAction = transition.getLabel();
			if (transitionAction.update.equals(update)) {
				foundAction = transitionAction;
				foundState = transition.getDst();
				break;
			}
		}
		if (foundAction == null) {
			return Optional.empty();
		} else {
			return Optional.of(new Pair<>(foundAction, foundState));
		}
	}
}
