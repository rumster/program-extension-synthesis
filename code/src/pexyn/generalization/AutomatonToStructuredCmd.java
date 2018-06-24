package pexyn.generalization;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import bgu.cs.util.graph.MultiGraph;
import bgu.cs.util.graph.MultiGraph.Edge;
import bgu.cs.util.rel.HashRel2;
import bgu.cs.util.rel.Rel2;
import pexyn.Semantics.Cmd;
import pexyn.Semantics.Guard;
import pexyn.Semantics.Store;
import pexyn.StructuredSemantics;

/**
 * Attempts to convert automata to structured programs.
 * 
 * @author romanm
 */
public class AutomatonToStructuredCmd<StoreType extends Store, CmdType extends Cmd, GuardType extends Guard> {
	private final StructuredSemantics<StoreType, CmdType, GuardType> sem;
	private Automaton m;

	public AutomatonToStructuredCmd(StructuredSemantics<StoreType, CmdType, GuardType> sem) {
		this.sem = sem;
	}

	public void compress(Automaton m) {
		this.m = m;

		var worklist = new HashSet<>(m.getNodes());
		while (!worklist.isEmpty()) {
			var node = worklist.iterator().next();
			worklist.remove(node);
			if (m.containsNode(node)) {
				compressSeq(node, worklist);
			}
			if (m.containsNode(node)) {
				compressLoop(node, worklist);
			}
			if (m.containsNode(node)) {
				compressCondition(node, worklist);
			}
		}
	}

	private void compressCondition(State node, Set<State> worklist) {
		if (m.outDegree(node) < 2) {
			return;
		}

		Rel2<MultiGraph.Edge<State, Action>, State> edgeToDst = new HashRel2<>();
		for (var edge : m.succEdges(node)) {
			edgeToDst.add(edge, edge.dst);
		}
		for (var dst : edgeToDst.all2()) {
			Collection<Edge<State, Action>> edges = edgeToDst.select2(dst);
			if (edges.size() == 2) {
				var succEdgeIter = edges.iterator();
				var succEdge1 = succEdgeIter.next();
				var succEdge2 = succEdgeIter.next();
				var succNode = succEdge1.getDst();

				var newAction = new Action(sem.getTrue(), sem.condition(succEdge1.getLabel().guard(),
						succEdge1.getLabel().update, succEdge2.getLabel().update));
				m.removeEdge(succEdge1);
				m.removeEdge(succEdge2);
				m.addEdge(node, succNode, newAction);

				worklist.add(node);
				worklist.add(succNode);

				return;
			}
		}
	}

	private void compressLoop(State node, Set<State> worklist) {
		if (m.outDegree(node) != 2) {
			return;
		}
		var succEdgeIter = m.succEdges(node).iterator();
		var succEdge1 = succEdgeIter.next();
		var succEdge2 = succEdgeIter.next();
		var succNode1 = succEdge1.dst;
		var succNode2 = succEdge2.dst;
		// Test for a self-loop.
		if (succNode1 != node && succNode2 != node) {
			return;
		}

		// Swap edges, if necessary such that succEdge1 is the self-loop.
		var succEdge1Tmp = succEdge1;
		if (succNode2 == node) {
			succEdge1 = succEdge2;
			succEdge2 = succEdge1Tmp;
		}

		var selfLoopAction = succEdge1.label;
		var newAction = new Action(sem.getTrue(),
				sem.sequence(sem.loop(selfLoopAction.guard(), selfLoopAction.update), succEdge2.label.update));
		m.removeEdge(succEdge1);
		m.removeEdge(succEdge2);
		m.addEdge(node, succEdge2.dst, newAction);

		worklist.add(succNode1);
		worklist.add(succNode2);
	}

	private void compressSeq(State node, Set<State> worklist) {
		if (node == m.getInitial()) {
			// The initial location is special - it should
			// not be removed from the automaton.
			return;
		} else if (!m.containsNode(node) || m.outDegree(node) != 1 || m.inDegree(node) != 1) {
			return;
		}

		var prevEdge = m.predEdges(node).iterator().next();
		var prevNode = prevEdge.src;
		var prevAction = prevEdge.label;
		var succEdge = m.succEdges(node).iterator().next();
		var succNode = succEdge.dst;
		var succAction = succEdge.label;
		// Don't compress self-loops.
		if (node == prevNode || node == succNode) {
			return;
		}

		var newAction = new Action(prevAction.guard(), sem.sequence(prevAction.update, succAction.update));
		m.removeEdge(prevEdge);
		m.removeEdge(succEdge);
		m.removeNode(node);
		m.addEdge(prevNode, succNode, newAction);

		worklist.add(prevNode);
		worklist.add(succNode);
	}
}
