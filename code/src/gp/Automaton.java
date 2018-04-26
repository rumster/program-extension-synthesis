package gp;

import java.util.HashSet;
import java.util.Set;

import bgu.cs.util.graph.HashMultiGraph;

/**
 * A program automaton.
 * 
 * @author romanm
 *
 * @param <DataType>
 *            The type of data over which the automaton operates.
 * @param <UpdateType>
 *            The type of data operations over data elements.
 * @param <GuardType>
 *            The type of data predicates.
 */
public class Automaton<DataType, UpdateType, GuardType> extends
		HashMultiGraph<Automaton<DataType, UpdateType, GuardType>.State, Automaton<DataType, UpdateType, GuardType>.Action> {
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

	/**
	 * An automaton control state.
	 * 
	 * @author romanm
	 */
	public class State {
		Set<TraceContext> contexts = new HashSet<>();
	}

	/**
	 * An action labeling an automatont transition.
	 * 
	 * @author romanm
	 *
	 * @param <GuardType>
	 *            The type of condition enabling this action.
	 * @param <UpdateType>
	 *            The type of operation applied to an input data element.
	 */
	public class Action {
		public final GuardType guard;
		public final UpdateType update;

		public Action(GuardType guard, UpdateType update) {
			assert guard != null && update != null;
			this.guard = guard;
			this.update = update;
		}
	}

	/**
	 * A point along a trace.
	 * 
	 * @author romanm
	 *
	 * @param <DataType>
	 *            The type of data elements in the trace.
	 * @param <UpdateType>
	 *            The type of update operations in the trace.
	 */
	public class TraceContext {
		public final Plan<DataType, UpdateType> plan;
		public final int pos;

		public TraceContext(Plan<DataType, UpdateType> plan, int pos) {
			assert plan != null;
			assert pos >= 0 && pos < plan.size();
			this.plan = plan;
			this.pos = pos;
		}
	}
}
