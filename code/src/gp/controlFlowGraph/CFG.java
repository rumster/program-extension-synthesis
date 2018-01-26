package gp.controlFlowGraph;

import bgu.cs.util.graph.HashMultiGraph;

/**
 * A control-flow graph where nodes are associated with sets of states..
 * 
 * @author romanm
 *
 * @param <ActionType>
 *            The type of actions labeling edges.
 * @param <ConditionType>
 *            The type of conditions on branching nodes.
 */
public class CFG<StateType, ActionType, ConditionType>
		extends HashMultiGraph<CFG.Node, CFG.ConditionalAction<ActionType, ConditionType>> {
	public static final Node ENTRY = new Node() {
		@Override
		public String toString() {
			return "entry";
		}
	};

	public static final Node EXIT = new Node() {
		@Override
		public String toString() {
			return "exit";
		}
	};

	public final ConditionalAction<ActionType, ConditionType> skipEdge = new ConditionalAction<ActionType, ConditionType>(
			null) {
		@Override
		public ActionType action() {
			throw new IllegalArgumentException("A skip edge has no action associated with it!");
		}

		public ConditionType condition() {
			throw new IllegalArgumentException("A skip edge has no condition associated with it!");
		}

		public void setCondition(ConditionType condition) {
			throw new IllegalArgumentException("A skip edge is immutable!");
		}

		@Override
		public String toString() {
			return "skip";
		}
	};

	/**
	 * A node in the graph.
	 * 
	 * @author romanm
	 */
	public static class Node {
		private static int idCounter = 0;
		private int id = 0;

		public Node() {
			this.id = idCounter++;
		}

		@Override
		public String toString() {
			return "N" + id;
		}
	}

	public static class ConditionalAction<ActionType, ConditionType> {
		private final ActionType action;
		private ConditionType condition;

		public ConditionalAction(ActionType action) {
			this.action = action;
		}

		public ActionType action() {
			return action;
		}

		public ConditionType condition() {
			return condition;
		}

		public void setCondition(ConditionType condition) {
			this.condition = condition;
		}

		@Override
		public String toString() {
			if (condition != null) {
				return condition.toString() + "/" + action.toString();
			} else {
				return action.toString();
			}
		}
	}

	public CFG() {
		addNode(ENTRY);
		addNode(EXIT);
	}

	public void mergeNodes(Node from, Node to) {
		if (from == to) {
			return;
		}
		if (from == ENTRY || from == EXIT) {
			throw new IllegalArgumentException("Attempt to merge " + from.toString() + " into " + to.toString());
		}

	}
}