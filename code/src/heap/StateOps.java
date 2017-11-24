package heap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bgu.cs.util.graph.HashMultiGraph;
import bgu.cs.util.graph.MultiGraph;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import heap.State.Obj;
import heap.State.Val;

/**
 * State-related utility methods.
 * 
 * @author romanm
 */
public class StateOps {
	/**
	 * Returns a multigraph whose nodes are the objects of the state and the edges
	 * are labeled by the corresponding {@link RefField} fields.
	 */
	public static MultiGraph<Obj, RefField> stateToObjMultiGraph(State state) {
		HashMultiGraph<Obj, RefField> result = new HashMultiGraph<>();
		for (Obj o : state.getObjects()) {
			result.addNode(o);
		}
		for (Obj o : state.getObjects()) {
			for (Map.Entry<Field, Val> fieldEdge : state.geFields(o).entrySet()) {
				Field field = fieldEdge.getKey();
				if (field instanceof RefField) {
					RefField refField = (RefField) field;
					Obj succ = (Obj) fieldEdge.getValue();
					result.addEdge(o, succ, refField);
				}
			}
		}
		return result;
	}

	public static boolean reachableObjects(State state) {
		return reachableObjects(state, new LinkedList<Var>());
	}

	public static boolean reachableObjects(State state, Collection<Var> excludeSet) {
		int reachableObjects = StateOps.search(state, false, excludeSet).size();
		int totalObjects = state.getObjects().size();
		return (reachableObjects == totalObjects);
	}

	public static List<Obj> dfs(State state) {
		return search(state, true);
	}

	public static List<Obj> bfs(State state) {
		return search(state, false);

	}

	public static List<Obj> search(State state, boolean depth) {
		return search(state, depth, new LinkedList<Var>());
	}

	public static List<Obj> search(State state, boolean depth, Collection<Var> excludeSet) {
		List<Obj> result = new ArrayList<>(state.getObjects().size());
		LinkedList<Obj> open = new LinkedList<>();

		for (Map.Entry<Var, Val> valuation : state.getEnvMap().entrySet()) {
			Var var = valuation.getKey();
			Val v = valuation.getValue();

			if (!excludeSet.contains(var) && v instanceof Obj && v != Obj.NULL) {
				if (depth)
					open.addFirst((Obj) v);
				else
					open.addLast((Obj) v);
			}
		}
		Set<Obj> closed = new HashSet<>();
		while (!open.isEmpty()) {
			Obj o = open.removeFirst();
			if (closed.contains(o) || o == Obj.NULL)
				continue;
			closed.add(o);
			result.add(o);
			for (Field field : o.type.fields) {
				if (field instanceof RefField) {
					RefField refField = (RefField) field;
					Obj succ = state.eval(o, refField);
					if (succ != Obj.NULL && !closed.contains(succ)) {
						if (depth)
							open.addFirst(succ);
						else
							open.addLast(succ);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Returns a map associating each object with its distance from the given set of
	 * source objects.
	 * 
	 * @param state
	 *            A state.
	 * @param sources
	 *            A set of objects in the given state.
	 */
	public static TObjectIntMap<Obj> bfsMap(State state, Set<Obj> sources) {
		Set<Obj> open = sources;
		TObjectIntHashMap<Obj> result = new TObjectIntHashMap<>(state.objects.size(), 0.7f, -1);
		for (Obj o : sources) {
			result.put(o, 0);
		}
		Set<Obj> closed = new HashSet<>();
		while (!open.isEmpty()) {
			Iterator<Obj> firstIt = open.iterator();
			Obj o = firstIt.next();
			firstIt.remove();
			if (closed.contains(o) || o == Obj.NULL)
				continue;
			closed.add(o);
			int dist = result.get(o);
			for (Field field : o.type.fields) {
				if (field instanceof RefField) {
					RefField refField = (RefField) field;
					Obj succ = state.eval(o, refField);
					int succDist = result.getNoEntryValue();
					if (succDist == -1) {
						result.put(succ, dist + 1);
					} else {
						int minDist = dist < succDist ? dist : succDist;
						result.put(succ, minDist);
					}
					if (succ != Obj.NULL && !closed.contains(succ)) {
						open.add(succ);
					}
				}
			}
		}
		return result;
	}
}