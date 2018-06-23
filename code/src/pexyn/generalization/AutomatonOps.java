package pexyn.generalization;

import java.util.HashSet;

import pexyn.Semantics.Cmd;
import pexyn.Semantics.Guard;
import pexyn.Semantics.Store;
import pexyn.StructuredSemantics;

/**
 * Provides useful operations over automata.
 * 
 * @author romanm
 */
public class AutomatonOps {
	public static <StoreType extends Store, CmdType extends Cmd, GuardType extends Guard> void compress(Automaton m,
			StructuredSemantics<StoreType, CmdType, GuardType> sem) {
		var nodes = new HashSet<>(m.getNodes());
		while (!nodes.isEmpty()) {
			var node = nodes.iterator().next();
			nodes.remove(node);
			if (node == m.getInitial()) {
				// The initial location is special - it should
				// not be removed from the automaton.
				continue;
			}
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
