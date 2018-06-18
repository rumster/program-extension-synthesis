package pexyn.generalization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

import bgu.cs.util.Pair;
import bgu.cs.util.graph.HashMultiGraph;
import bgu.cs.util.rel.HashRel2;
import pexyn.Domain.Cmd;

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

	/**
	 * Constructs an automaton with an initial state, a final state, and an empty
	 * set of transitions.
	 */
	public Automaton() {
		entry = new State("initial");
		exit = new State("final");
		addNode(entry);
		addNode(exit);
	}

	/**
	 * Returns the initial state.
	 */
	public State getInitial() {
		return entry;
	}

	/**
	 * Returns the final state.
	 */
	public State getFinal() {
		return exit;
	}

	/**
	 * Returns a deep copy of this automaton.
	 */
	@Override
	public Automaton clone() {
		var result = new Automaton();
		var newStates = new ArrayList<State>(this.getNodes().size());
		var oldStateToNewState = new HashMap<State, State>(this.getNodes().size());
		// Create a copy of each state with a copy of the trace points.
		for (var oldState : getNodes()) {
			final State newState;
			if (oldState == getInitial()) {
				newState = result.getInitial();
			} else if (oldState == getFinal()) {
				newState = result.getFinal();
			} else {
				newState = new State(oldState.id);
			}
			newStates.add(newState);
			newState.addAllTracePoints(oldState.getPoints());
			result.addNode(newState);
			oldStateToNewState.put(oldState, newState);
		}

		// Now copy the transitions over to the new automaton.
		for (var oldState : getNodes()) {
			for (Edge<State, Action> transition : this.succEdges(oldState)) {
				var newSrc = oldStateToNewState.get(transition.getSrc());
				var newDst = oldStateToNewState.get(transition.getDst());
				var newAction = transition.getLabel().clone();
				result.addEdge(newSrc, newDst, newAction);
			}
		}

		return result;
	}

	/**
	 * Finds a transition outgoing from the given state and labelled with the given
	 * update.
	 */
	public Optional<Pair<Action, State>> findTransition(State src, Cmd update) {
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

	/**
	 * Tests whether all states are update-deterministic.
	 */
	public boolean isUpdateDeterministic() {
		for (final var state : this.getNodes()) {
			if (!isUpdateDeterministic(state)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Tests whether all outgoing transitions are labeled by unique updates.
	 */
	public boolean isUpdateDeterministic(State state) {
		final var stateUpdates = new HashSet<Cmd>();
		for (final var transition : this.succEdges(state)) {
			final var freshUpdate = stateUpdates.add(transition.getLabel().update);
			if (!freshUpdate) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Recursively makes states update-deterministic, starting from the given state
	 * up to states that are already update-deterministic.
	 */
	public void fold(State state) {
		removeDuplicateUpdates(state);
		if (!containsNode(state) || state == getFinal() || isUpdateDeterministic(state)) {
			return;
		}
		var affected = makeUpdateDeterministic(state);
		if (affected.isEmpty()) {
			return;
		}
		for (var affectedState : affected) {
			fold(affectedState);
			removeDuplicateUpdates(affectedState);
		}
	}

	/**
	 * Merges states in order to ensure that the transitions outgoing from the given
	 * state are update-deterministic.
	 * 
	 * @return The states possibly affected by this transformation.
	 */
	public Collection<State> makeUpdateDeterministic(State state) {
		if (!containsNode(state) || isUpdateDeterministic(state)) {
			return Collections.emptyList();
		}
		var result = new ArrayList<State>();
		result.add(state);

		var updateToTargetState = new HashRel2<Cmd, State>();
		for (Edge<State, Action> transition : this.succEdges(state)) {
			var transitionAction = transition.getLabel();
			updateToTargetState.add(transitionAction.update, transition.getDst());
		}
		for (var update : updateToTargetState.all1()) {
			var statesToFold = updateToTargetState.select1(update);
			var optMergedState = mergeStates(statesToFold);
			if (optMergedState.isPresent()) {
				var mergedState = optMergedState.get();
				result.add(mergedState);
			}
		}

		return result;
	}

	/**
	 * Removes an outgoing edge if there is already a parallel one with the same
	 * update.
	 */
	protected void removeDuplicateUpdates(State state) {
		if (!containsNode(state)) {
			return;
		}
		var updateToAction = new HashRel2<Cmd, Edge<State, Action>>();
		for (var transition : this.succEdges(state)) {
			var transitionAction = transition.getLabel();
			updateToAction.add(transitionAction.update, transition);
		}
		for (var update : updateToAction.all1()) {
			var updateEdges = updateToAction.select1(update);
			var uniqueEdges = new ArrayList<Edge<State, Action>>();
			for (var edge : updateEdges) {
				var change = bgu.cs.util.Collections.addNoEquiv(uniqueEdges, edge,
						(e1, e2) -> e1.getSrc() == e2.getSrc() && e1.getDst() == e2.getDst());
				if (!change) {
					super.removeEdge(edge);
				}
			}
		}
	}

	/**
	 * Destructively merges all states in the given collection into the first one
	 * (the first one returned by an iterator over the given collection). None of
	 * which can be the final state. States that are not in the automaton are
	 * ignored.
	 * 
	 * @return The state into which all states where merged or empty if all states
	 *         in the collection were not in the automaton.
	 */
	public Optional<State> mergeStates(Collection<State> states) {
		var statesList = new ArrayList<State>();
		for (var state : states) {
			if (containsNode(state)) {
				bgu.cs.util.Collections.addNoCopies(statesList, state);
			}
		}
		if (statesList.size() == 0) {
			return Optional.empty();
		}
		if (states.size() == 1) {
			return Optional.of(states.iterator().next());
		}

		// Ensure that if the list includes the initial state then it appears first
		// so that we don't attempt to merge it into another state.
		for (int i = 0; i < statesList.size(); ++i) {
			if (statesList.get(i) == this.getInitial()) {
				Collections.swap(statesList, i, 0);
				break;
			}
		}
		var firstState = statesList.get(0);
		for (int i = 1; i < statesList.size(); ++i) {
			var otherState = statesList.get(i);
			this.mergeStates(otherState, firstState);
		}

		return Optional.ofNullable(firstState);
	}

	/**
	 * Destructively merges the first state into the second, resulting in an
	 * automaton that is not necessarily update-deterministic.<br>
	 * Precondition: 1) Neither of the states may be the final state.<br>
	 * 2) The first state may not be the initial state.
	 */
	public void mergeStates(State src, State dst) {
		if (src == this.getFinal() || dst == this.getFinal()) {
			throw new Error("Attempt to merge states including a final state!");
		}
		if (src == this.getInitial()) {
			throw new Error("Attempt to merge the initial state into another state!");
		}

		if (src == dst) {
			return;
		}

		dst.addAllTracePoints(src.getPoints());
		super.mergeInto(src, dst);
		assert !containsNode(src);
		assert containsNode(dst);
	}

	/**
	 * Returns the predecessor states of the given state.
	 */
	public Collection<State> predStates(State state) {
		var result = new ArrayList<State>();
		for (var edge : predEdges(state)) {
			bgu.cs.util.Collections.addNoCopies(result, edge.getSrc());
		}
		return result;
	}

	/**
	 * Returns the successor states of the given state.
	 */
	public Collection<State> srccStates(State state) {
		var result = new ArrayList<State>();
		for (var edge : succEdges(state)) {
			bgu.cs.util.Collections.addNoCopies(result, edge.getDst());
		}
		return result;
	}
}
