package pexyn.generalization;

import java.util.ArrayList;

import pexyn.Semantics;
import pexyn.Semantics.Cmd;
import pexyn.Semantics.Guard;
import pexyn.Semantics.Store;

public class AutomatonOps {
	public static <StoreType extends Store, CmdType extends Cmd, GuardType extends Guard> void shrinkBlocks(Automaton m,
			Semantics<StoreType, CmdType, GuardType> sem) {
		var nodes = new ArrayList<>(m.getNodes());
		for (var node : nodes) {
			if (!m.containsNode(node) || m.outDegree(node) != 1 || m.inDegree(node) != 1) {
				continue;
			}
			var prevEdge = m.predEdges(node).iterator().next();
			var prevNode = prevEdge.src;
			var prevAction = prevEdge.label;
			var succEdge = m.succEdges(node).iterator().next();
			var succNode = succEdge.dst;
			var succAction = succEdge.label;
			if (node == prevNode || node == succNode) {
				continue;
			}

			var newAction = new Action(prevAction.guard(), sem.sequence(prevAction.update, succAction.update));
			m.removeEdge(prevEdge);
			m.removeEdge(succEdge);
			m.removeNode(node);
			m.addEdge(prevNode, succNode, newAction);
		}
	}
}
