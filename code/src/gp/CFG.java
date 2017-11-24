package gp;

import bgu.cs.util.graph.HashMultiGraph;

/**
 * A control-flow graph.
 * 
 * @author romanm
 *
 * @param <ActionType>
 *            The type of actions labeling edges.
 * @param <ConditionType>
 *            The type of conditions on branching nodes.
 */
public class CFG<ActionType, ConditionType> extends HashMultiGraph<CFG.Node, CFG.Edge<ActionType, ConditionType>> {
	public static final Node ENTRY = new Node();
	public static final Node EXIT = new Node();

	public static class Node {
	}

	public static class Edge<ActionType, ConditionType> {
		public final ActionType action;
		public ConditionType condition;

		public Edge(ActionType action) {
			this.action = action;
		}
	}

	public CFG() {
		addNode(ENTRY);
		addNode(EXIT);
	}
}