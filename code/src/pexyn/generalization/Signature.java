package pexyn.generalization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bgu.cs.util.graph.MultiGraph.Edge;
import bgu.cs.util.rel.HashRel2;
import bgu.cs.util.rel.Rel2;
import pexyn.Domain.Update;

/**
 * A set of lookaheads for a given length.
 * 
 * @author romanm
 */
public class Signature {
	public final int length;
	public Set<List<Update>> lookaheads;

	public Signature(final Set<List<Update>> lookaheads, final int length) {
		this.lookaheads = lookaheads;
		this.length = length;
	}

	@Override
	public String toString() {
		var result = new StringBuilder();
		result.append("{length=" + length + ", " + lookaheads + "}");
		return result.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		final Signature other = (Signature) o;
		return this.length == other.length && this.lookaheads.equals(other.lookaheads);
	}

	@Override
	public int hashCode() {
		var result = 31 * length;
		result = result * 31 + lookaheads.hashCode();
		return result;
	}

	public boolean subset(Signature other, boolean proper) {
		// An optimization check.
		if (this.lookaheads.size() > other.lookaheads.size()) {
			return false;
		}

		var result = other.lookaheads.containsAll(this.lookaheads);
		if (proper) {
			result &= this.lookaheads.size() < other.lookaheads.size();
		}
		return result;
	}

	public static Rel2<State, Signature> getSignatures(Automaton automaton, int lookaheadLength) {
		var result = new HashRel2<State, Signature>();
		for (var state : automaton.getNodes()) {
			if (state == automaton.getFinal()) {
				continue;
			}
			var signature = Signature.from(automaton, state, lookaheadLength);
			result.add(state, signature);
		}
		return result;
	}

	public static Signature from(final Automaton m, final State s, final int length) {
		var lookaheadList = getLookaheads(m, s, length);
		var lookaheads = new HashSet<List<Update>>(lookaheadList);
		var result = new Signature(lookaheads, length);
		return result;
	}

	protected static Collection<List<Update>> getLookaheads(final Automaton automaton, State s, final int length) {
		if (s == automaton.getFinal()) {
			var finalStateUpdateList = new ArrayList<Update>(length);
			bgu.cs.util.Collections.addCopies(finalStateUpdateList, HaltUpdate.v, length);
			Collection<List<Update>> result = Collections.singletonList(finalStateUpdateList);
			return result;
		} else {
			return getLookaheadsHelper(automaton, s, length, length);
		}
	}

	protected static Collection<List<Update>> getLookaheadsHelper(final Automaton automaton, State s, final int length,
			final int k) {
		assert k >= 1;
		var result = new ArrayList<List<Update>>();
		if (s == automaton.getFinal()) {
			var singleUpdateList = new ArrayList<Update>(length);
			bgu.cs.util.Collections.addCopies(singleUpdateList, HaltUpdate.v, length);
			result.add(singleUpdateList);
		} else if (k == 1) {
			for (Edge<State, Action> transition : automaton.succEdges(s)) {
				Action transitionAction = transition.getLabel();
				var singleUpdateList = new ArrayList<Update>(length);
				bgu.cs.util.Collections.addCopies(singleUpdateList, HaltUpdate.v, length);
				singleUpdateList.set(length - k, transitionAction.update);
				result.add(singleUpdateList);
			}
		} else {
			for (Edge<State, Action> transition : automaton.succEdges(s)) {
				var succLookaheads = getLookaheadsHelper(automaton, transition.getDst(), length, k - 1);
				var transitionupdate = transition.getLabel().update;
				for (var succLookahead : succLookaheads) {
					succLookahead.set(length - k, transitionupdate);
				}
				result.addAll(succLookaheads);
			}
		}
		return result;
	}
}
