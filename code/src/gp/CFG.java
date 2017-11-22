package gp;

import java.util.Collection;
import java.util.Optional;

import bgu.cs.util.graph.HashMultiGraph;

/**
 * A control-flow graph.
 * 
 * @author romanm
 *
 * @param <StateType>
 *            The type of states over which computation is performed.
 * @param <ActionType>
 *            The type of actions labeling edges.
 * @param <ConditionType>
 *            The type of conditions on branching nodes.
 */
public class CFG<StateType, ActionType, ConditionType>
		extends HashMultiGraph<CFG.Node, CFG.Edge<ActionType, ConditionType>>
		implements DTR<CFG.Node, CFG.Edge<ActionType, ConditionType>> {
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

	@Override
	public Collection<Edge<ActionType, ConditionType>> enabledActions(Node state) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float transitionCost(Node src, Edge<ActionType, ConditionType> action, Node dst) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Collection<Node> apply(Node state, Edge<ActionType, ConditionType> action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Edge<ActionType, ConditionType>> enabledAction(Node state) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Node> next(Node state) {
		// TODO Auto-generated method stub
		return null;
	}
}