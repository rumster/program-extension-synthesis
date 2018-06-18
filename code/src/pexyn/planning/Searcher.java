package pexyn.planning;

import java.util.function.Predicate;

/**
 * An algorithm for searching a transition relation for a state satisfying a
 * given goal.
 * 
 * @author romanm
 *
 * @param <StateType>
 *            The type of states in the state space.
 * @param <ActionType>
 *            The type of actions that label transitions in the transition
 *            relation.
 */
public interface Searcher<StateType, ActionType> {
	/**
	 * The result of a search.
	 * 
	 * @author romanm
	 *
	 * @param <StateType>
	 */
	public static class SearchResult<StateType> {
		private final StateType goalNode;
		private final SearchResultType resultType;

		protected SearchResult(StateType goalNode, SearchResultType resultType) {
			this.goalNode = goalNode;
			this.resultType = resultType;
		}

		public static <StateType> SearchResult<StateType> of(StateType goalState) {
			return new SearchResult<StateType>(goalState, SearchResultType.OK);
		}

		public static <StateType> SearchResult<StateType> noSolutionExists() {
			return new SearchResult<StateType>(null, SearchResultType.NO_SOLUTION_EXISTS);
		}

		public static <StateType> SearchResult<StateType> outOfResources() {
			return new SearchResult<StateType>(null, SearchResultType.OUT_OF_RESOURCES);
		}

		public SearchResultType resultType() {
			return resultType;
		}

		public boolean fail() {
			return resultType == SearchResultType.NO_SOLUTION_EXISTS || resultType == SearchResultType.OUT_OF_RESOURCES;
		}

		public boolean found() {
			return resultType == SearchResultType.OK;
		}

		public StateType get() {
			if (goalNode == null) {
				throw new Error("Cannot invoke get on an empty Result object!");
			} else {
				return goalNode;
			}
		}
	}

	/**
	 * Attempts state that is both reachable from the initial state and satisfies
	 * the goal.
	 * 
	 * @param input
	 *            The start state.
	 * @param goalTest
	 *            A predicate that tests whether a state satisfies the goal
	 *            condition.
	 * @return The result of the search.
	 */
	public SearchResult<StateType> findState(StateType input, Predicate<StateType> goalTest);
}