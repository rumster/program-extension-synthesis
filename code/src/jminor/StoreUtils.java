package jminor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.stringtemplate.v4.ST;

import bgu.cs.util.FileUtils;
import bgu.cs.util.Pair;
import bgu.cs.util.STGLoader;
import bgu.cs.util.Tuple3;
import bgu.cs.util.graph.HashMultiGraph;
import bgu.cs.util.graph.MultiGraph;
import bgu.cs.util.graph.visualization.GraphizVisualizer;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import jminor.JmStore.JmErrorStore;

/**
 * Store-related utility methods.
 * 
 * @author romanm
 */
public class StoreUtils {
	protected static STGLoader templates = new STGLoader(JmStore.class);

	private static Map<Obj, String> objToName = new HashMap<>();

	private static String getObjName(Obj o) {
		String result = objToName.get(o);
		if (result == null) {
			result = o.type.name + "#" + objToName.size();
			objToName.put(o, result);
		}
		return result;
	}

	public static boolean typecheck(Var var, Val v) {
		return (var instanceof IntVar && v instanceof IntVal)
				|| (var instanceof RefVar && (v == null || v instanceof Obj));
	}

	public static boolean typecheck(Field field, Val v) {
		return (field instanceof IntField && v instanceof IntVal)
				|| (field instanceof RefField && (v == null || v instanceof Obj));
	}

	public static boolean typecheck(Field field, Obj o) {
		return (field.srcType.equals(o.type));
	}

	/**
	 * Renders the given store into an image file with the given base name.
	 */
	public static void printStore(JmStore store, String filename, Logger logger) {
		String dotStr;
		if (store instanceof JmErrorStore) {
			JmErrorStore errorStore = (JmErrorStore) store;
			var errorStoreST = templates.load("ErrorStore");
			errorStoreST.add("message", errorStore.description);
			dotStr = errorStoreST.render();
		} else {
			dotStr = storeToDOT(store);
		}
		GraphizVisualizer.renderToFile(dotStr, FileUtils.base(filename), FileUtils.suffix(filename), logger);
	}

	/**
	 * Returns a representation of the given store in the DOT (graph language)
	 * format.
	 */
	public static String storeToDOT(JmStore store) {
		ST template = templates.load("StoreDOT");

		// Assign objects names and render their non-reference values.
		Map<Obj, String> objToDotNodeName = new HashMap<>();
		objToDotNodeName.put(Obj.NULL, "null");
		int i = 0;
		for (Obj o : store.getObjects()) {
			String objName = getObjName(o);
			String dotNodeName = "N" + i;
			objToDotNodeName.put(o, dotNodeName);
			ST objContent = templates.load("objectContent");
			objContent.add("name", objName);
			for (Field f : o.type.fields) {
				if (store.isInitialized(o, f) && !(f instanceof RefField)) {
					objContent.add("vals", f.name + "=" + store.eval(o, f).toString());
				}
			}
			template.add("objects", new Pair<String, String>(dotNodeName, objContent.render()));
			++i;
		}

		// Assign a node to each variable.
		Map<RefVar, String> refVarToDotNodeName = new HashMap<>();
		for (Var var : store.getEnvMap().keySet()) {
			if (var instanceof RefVar) {
				RefVar refVar = (RefVar) var;
				refVarToDotNodeName.put(refVar, var.name);
				template.add("refVarNodes", var.name);
			} else {
				if (store.isInitialized(var)) {
					template.add("nonRefVarVals",
							new Pair<String, String>(var.name, var.name + "==" + store.eval(var)));
				}
			}
		}

		// Add arrows from reference variables to objects.
		store.getEnvMap().forEach((var, val) -> {
			if (store.isInitialized(var) && var instanceof RefVar) {
				RefVar refVar = (RefVar) var;
				Obj o = (Obj) val;
				template.add("refVarVals",
						new Pair<String, String>(refVarToDotNodeName.get(refVar), objToDotNodeName.get(o)));
			}
		});

		// Add arrows for reference fields.
		for (Obj src : store.getObjects()) {
			String srcName = objToDotNodeName.get(src);
			for (Field f : src.type.fields) {
				if (store.isInitialized(src, f) && f instanceof RefField) {
					RefField refField = (RefField) f;
					Obj dst = store.eval(src, refField);
					String dstName = objToDotNodeName.get(dst);
					template.add("refFields", new Tuple3<String, String, String>(srcName, dstName, refField.name));
				}
			}
		}

		return template.render();
	}

	/**
	 * Returns a multigraph whose nodes are the objects of the store and the edges
	 * are labeled by the corresponding {@link RefField} fields.
	 */
	public static MultiGraph<Obj, RefField> storeToObjMultiGraph(JmStore store) {
		HashMultiGraph<Obj, RefField> result = new HashMultiGraph<>();
		result.addNode(Obj.NULL);
		for (Obj o : store.getObjects()) {
			result.addNode(o);
		}

		for (Obj o : store.getObjects()) {
			for (Map.Entry<Field, Val> fieldEdge : store.geFields(o).entrySet()) {
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

	public static boolean reachableObjects(JmStore state) {
		return reachableObjects(state, new LinkedList<Var>());
	}

	public static boolean reachableObjects(JmStore state, Collection<Var> excludeSet) {
		int reachableObjects = StoreUtils.search(state, false, excludeSet).size();
		int totalObjects = state.getObjects().size();
		return (reachableObjects == totalObjects);
	}

	public static List<Obj> dfs(JmStore store) {
		return search(store, true);
	}

	public static List<Obj> bfs(JmStore store) {
		return search(store, false);

	}

	public static List<Obj> search(JmStore store, boolean depth) {
		return search(store, depth, new LinkedList<Var>());
	}

	public static List<Obj> search(JmStore store, boolean depth, Collection<Var> excludeSet) {
		List<Obj> result = new ArrayList<>(store.getObjects().size());
		LinkedList<Obj> open = new LinkedList<>();

		for (Map.Entry<Var, Val> valuation : store.getEnvMap().entrySet()) {
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
					Obj succ = store.eval(o, refField);
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
	 * @param store
	 *            A store.
	 * @param sources
	 *            A set of objects in the given store.
	 */
	public static TObjectIntMap<Obj> bfsMap(JmStore store, Set<Obj> sources) {
		Set<Obj> open = sources;
		TObjectIntHashMap<Obj> result = new TObjectIntHashMap<>(store.objects.size(), 0.7f, -1);
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
					Obj succ = store.eval(o, refField);
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