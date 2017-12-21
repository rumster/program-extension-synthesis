package gp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import bgu.cs.util.BucketHeap;

/**
 * An implementation of the A* search algorithm.
 * 
 * @author romanm
 *
 * @param <StateType>
 *            The type of states in the state space.
 * @param <ActionType>
 *            The type of actions in the transition relation.
 */
public class AStar<StateType, ActionType> implements Planner<StateType, ActionType>, Searcher<StateType, ActionType> {
	/**
	 * The transition relation over which the search is performed.
	 */
	protected TR<StateType, ActionType> tr;

	/**
	 * Constructs an instance of the algorithm for the given transition system.
	 */
	public AStar(TR<StateType, ActionType> tr) {
		this.tr = tr;
	}

	@Override
	public SearchResultType findPlan(StateType input, Predicate<StateType> goalTest,
			Plan<StateType, ActionType> addToPlan) {
		Node<StateType, ActionType> resultNode = searchNode(input, goalTest);
		if (resultNode != null) {
			createPath(resultNode, addToPlan);
			return SearchResultType.OK;
		} else {
			return SearchResultType.NO_SOLUTION_EXISTS;
		}
	}

	@Override
	public SearchResult<StateType> findState(StateType initial, Predicate<StateType> goalTest) {
		Node<StateType, ActionType> goalNode = searchNode(initial, goalTest);
		if (goalNode == null) {
			return SearchResult.noSolutionExists();
		} else {
			return SearchResult.of(goalNode.state);
		}
	}

	/**
	 * A standard A*-based planning algorithm.
	 * 
	 * @param initial
	 *            The initial state.
	 * @param goalTest
	 *            The predicate expressing the goal states.
	 * @return The resulting search node or null if no plan exists.
	 */
	protected Node<StateType, ActionType> searchNode(StateType initial, Predicate<StateType> goalTest) {
		Map<StateType, Node<StateType, ActionType>> stateToNode = new HashMap<>();
		BucketHeap<Float, Node<StateType, ActionType>> open = new BucketHeap<>();

		Node<StateType, ActionType> startstate = new Node<>(initial, null);
		stateToNode.put(initial, startstate);
		startstate.gscore = 0;
		startstate.fscore = tr.estimateDistToGoal(initial);
		open.put(startstate.fscore, startstate);

		while (!open.isEmpty()) {
			Node<StateType, ActionType> current = open.pop();

			StateType currentState = current.state;
			if (goalTest.test(currentState)) {
				// Found a solution.
				return current;
			}

			current.closed = true;

			for (ActionType action : tr.enabledActions(currentState)) {
				for (StateType nextState : tr.apply(currentState, action)) {
					Node<StateType, ActionType> neighborNode = stateToNode.get(nextState);
					if (neighborNode == null) {
						// This is a never before seen state.
						neighborNode = new Node<>(nextState, current);
						float tentativeGScore = current.gscore + tr.transitionCost(currentState, action, nextState);
						neighborNode.parent = current;
						neighborNode.computedFrom = action;
						neighborNode.gscore = tentativeGScore;
						neighborNode.fscore = tentativeGScore + tr.estimateDistToGoal(nextState);
						open.put(neighborNode.gscore, neighborNode);
						stateToNode.put(nextState, neighborNode);
					} else {
						if (neighborNode.closed)
							continue;
						float tentativeGScore = current.gscore + tr.transitionCost(currentState, action, nextState);
						if (tentativeGScore >= neighborNode.gscore)
							continue;
						neighborNode.parent = current;
						neighborNode.computedFrom = action;
						neighborNode.gscore = tentativeGScore;
						neighborNode.fscore = tentativeGScore + tr.estimateDistToGoal(nextState);
						open.put(neighborNode.gscore, neighborNode);
					}
				}
			}
		}

		// The search has failed. No solution exists.
		return null;
	}

	/**
	 * Uses the 'computedFrom' back links to construct the path from the initial
	 * node to the given node.
	 * 
	 * TODO: make the plan concatenation more efficient.
	 * 
	 * @param node
	 *            A search node.
	 */
	protected void createPath(final Node<StateType, ActionType> node, Plan<StateType, ActionType> addToPlan) {
		ArrayList<ActionType> actions = new ArrayList<>();
		ArrayList<StateType> states = new ArrayList<>();
		states.add(node.state);
		Node<StateType, ActionType> pathNode = node;
		while (pathNode.parent != null) {
			actions.add(pathNode.computedFrom);
			states.add(pathNode.parent.state);
			pathNode = pathNode.parent;
		}
		Collections.reverse(actions);
		Collections.reverse(states);
		Plan<StateType, ActionType> result = new ArrayListPlan<>(states, actions);
		addToPlan.appendPlan(result);
	}

	/**
	 * A node in the search graph.
	 * 
	 * @author romanm
	 *
	 * @param <StateType>
	 *            The type of states in the state space.
	 * @param <ActionType>
	 *            The type of actions in the transition relation.
	 */
	protected static class Node<StateType, ActionType> {
		public static final float MAX_SCORE = Float.POSITIVE_INFINITY;

		/**
		 * The state encapsulated in this node.
		 */
		public StateType state;

		/**
		 * A predecessor node in the search graph.
		 */
		public Node<StateType, ActionType> parent;

		/**
		 * The action used to compute the parent node.
		 */
		public ActionType computedFrom;

		/**
		 * Indicates that this state has been fully explored.
		 */
		public boolean closed = false;

		/**
		 * The cost of the path from the start node to this node.
		 */
		public float gscore = MAX_SCORE;

		/**
		 * A utility score consisting of the gscore and the heuristic estimate to the
		 * goal.
		 */
		public float fscore = MAX_SCORE;

		public Node(StateType s, Node<StateType, ActionType> p) {
			this.state = s;
			this.parent = p;
			this.computedFrom = null;
		}
	}
}