package jminor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.stringtemplate.v4.ST;

import bgu.cs.util.STGLoader;
import bgu.cs.util.treeGrammar.Node;
import pexyn.Semantics.ErrorStore;
import pexyn.Semantics.Store;

/**
 * Represents a concrete store of a Jminor program. Uninitialized variables and
 * fields are treated as null.
 * 
 * @author romanm
 */
public class JmStore implements Store {
	public static final STGLoader templates = new STGLoader(JmStore.class, JmStore.class.getSimpleName());

	/**
	 * The set of allocated objects.
	 */
	protected final Set<Obj> objects;

	protected final Set<Obj> freeObjects;

	/**
	 * A map assigning variables to values.
	 */
	protected final Map<Var, Val> env;

	/**
	 * A map assigning each object and reference field to the object it references
	 * or null.
	 */
	protected final Map<Obj, Map<Field, Val>> heap;

	public static JmStore error(String description) {
		return new JmErrorStore(description);
	}

	/**
	 * Constructs an empty store.
	 */
	public JmStore() {
		this.objects = new HashSet<>();
		this.freeObjects = Set.of();
		this.env = new HashMap<>();
		this.heap = new HashMap<>();
		this.heap.put(Obj.NULL, Collections.emptyMap());
	}

	/**
	 * Constructs a store with the specified set of objects, environment, and heap.
	 * 
	 * @param objects
	 *            The set of allocated objects.
	 * @param freeObjects
	 *            The objects that are not currently allocated.
	 * @param env
	 *            An environment mapping variables to fields.
	 * @param heap
	 *            A heap mapping objects and fields to other objects.
	 */
	public JmStore(Set<Obj> objects, Set<Obj> freeObjects, Map<Var, Val> env, Map<Obj, Map<Field, Val>> heap) {
		assert objects != null && !objects.contains(Obj.NULL);
		assert freeObjects != null && !freeObjects.contains(Obj.NULL);
		assert Collections.disjoint(objects, freeObjects);
		this.objects = objects;
		this.freeObjects = freeObjects;
		this.env = env;
		this.heap = heap;
	}

	public Map<Var, Val> getEnvMap() {
		return env;
	}

	public Map<Field, Val> geFields(Obj obj) {
		assert objects.contains(obj);
		Map<Field, Val> result = heap.get(obj);
		if (result == null) {
			result = Collections.emptyMap();
		}
		return result;
	}

	/**
	 * @return The set of allocated objects.
	 */
	public Collection<Obj> getObjects() {
		return objects;
	}

	/**
	 * Compares two stores.<br>
	 * WARNING: the current implementation does not work correctly when object
	 * allocation has been used, since that requires an isomorphism check.
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof JmStore))
			return false;
		if (this == o)
			return true;
		JmStore other = (JmStore) o;
		if (!objects.equals(other.objects))
			return false;
		if (!freeObjects.equals(other.freeObjects))
			return false;
		if (!env.equals(other.env))
			return false;
		if (!heap.equals(other.heap))
			return false;
		return true;
	}

	public boolean equalHeap(JmStore other) {
		assert objects.equals(other.objects);
		return heap.equals(other.heap);
	}

	public boolean equalEnv(JmStore other) {
		return env.equals(other.env);
	}

	@Override
	public int hashCode() {
		int result = objects.hashCode();
		result = result * 31 + env.hashCode();
		result = result * 31 + heap.hashCode();
		return result;
	}

	/**
	 * Returns the store obtained by unmapping the given set of variables and
	 * performing garbage collections.
	 * 
	 * @param deadVars
	 *            A collection of out-of-scope variables.
	 */
	public JmStore clean(Iterable<Var> deadVars) {
		JmStore result = new JmStore(new HashSet<>(this.objects), Set.of(), new HashMap<>(this.env), this.heap);
		for (Var deadVar : deadVars) {
			result.env.remove(deadVar);
		}
		result = result.removeGarbage();
		return result;
	}

	public boolean containsGarbage() {
		int numObjects = objects.size();
		JmStore gfree = removeGarbage();
		int numObjectsAfterGC = gfree.getObjects().size();
		return numObjects != numObjectsAfterGC;
	}

	public boolean isInitialized(Var var) {
		return env.containsKey(var);
	}

	public boolean isInitialized(Object obj, Field field) {
		Map<Field, Val> objFields = heap.get(obj);
		return objFields != null && objFields.containsKey(field);
	}

	/**
	 * Evaluates an expression in this store returning its value or null if the
	 * expression is illegal (e.g., a null dereference or an access to an
	 * uninitialized variable).
	 */
	public Val eval(Node n) {
		if (n instanceof Var) {
			Var var = (Var) n;
			if (isInitialized(var)) {
				return eval(var);
			} else {
				throw new IllegalArgumentException("Attempt to evaluate an uninitialized variable " + var.name);
			}
		} else if (n instanceof DerefExpr) {
			DerefExpr deref = (DerefExpr) n;
			Node lhs = deref.getLhs();
			Val lval = eval(lhs);
			if (lval == null || lval == Obj.NULL) {
				throw new IllegalArgumentException("Attempt to evaluate an illegal expression " + deref.toString());
			}
			Obj lobj = (Obj) lval;
			Field field = deref.getField();
			return eval(lobj, field);
		} else if (n instanceof IntVal) {
			return (IntVal) n;
		} else {
			assert false : "Unexpected expression " + n + "!";
			return null;
		}
	}

	/**
	 * Returns the value of the given variable or null if it is not initialized.
	 */
	public Val eval(Var var) {
		return env.get(var);
	}

	/**
	 * Returns the object referenced by the given variable or null if it is not
	 * initialized.
	 */
	public Obj eval(RefVar var) {
		Obj result = (Obj) env.get(var);
		return result;
	}

	/**
	 * Returns the integer value of the given variable or null if it is not
	 * initialized.
	 */
	public IntVal eval(IntVar var) {
		IntVal result = (IntVal) env.get(var);
		return result;
	}

	/**
	 * Returns the value stored in the given field of the given object.
	 * 
	 * @param var
	 *            An object.
	 * @param field
	 *            A field.
	 */
	public Val eval(Obj obj, Field field) {
		Map<Field, Val> objFields = heap.get(obj);
		return objFields != null ? objFields.get(field) : null;
	}

	public Val eval(RefVar var, Field field) {
		Obj obj = eval(var);
		assert obj != null;
		return eval(obj, field);
	}

	/**
	 * Returns the object referenced by the access path 'obj.field'. If the dstType
	 * of variable and field do not match, the returned value is null. If obj is
	 * null - the 'illegal' object is returned.
	 * 
	 * @param obj
	 *            An object.
	 * @param field
	 *            A reference field.
	 * @return An object, null, or the 'illegal' object.
	 */
	public Obj eval(Obj obj, RefField field) {
		assert obj != null;
		Map<Field, Val> objFields = heap.get(obj);
		return (Obj) objFields.get(field);
	}

	public IntVal eval(Obj obj, IntField field) {
		assert obj != null;
		Map<Field, Val> objFields = heap.get(obj);
		return (IntVal) objFields.get(field);
	}

	/**
	 * Returns a free object of the given type, if there is one available and an
	 * empty result otherwise.
	 * 
	 * @param type
	 *            The type of the object to allocate.
	 */
	public Optional<Obj> allocate(RefType type) {
		for (Obj o : freeObjects) {
			if (o.type == type) {
				freeObjects.remove(o);
				return Optional.of(o);
			}
		}
		return Optional.empty();
	}

	/**
	 * Returns a new store, resulting from assigning the given variable to the given
	 * value.
	 */
	public JmStore assign(Var lvar, Val v) {
		assert lvar != null && v != null && StoreUtils.typecheck(lvar, v);
		Map<Var, Val> newEnv = new HashMap<>(this.env);
		newEnv.put(lvar, v);
		JmStore newState = new JmStore(this.objects, this.freeObjects, newEnv, this.heap);
		return newState;
	}

	public JmStore assign(Var t1, RefVar x) {
		return assign(t1, eval(x));
	}

	public JmStore assign(Obj lobj, Field field, Val v) {
		assert lobj != null && field != null && v != null;
		assert StoreUtils.typecheck(field, v);
		assert StoreUtils.typecheck(field, lobj);

		Map<Obj, Map<Field, Val>> newHeap = new HashMap<>(heap);
		Map<Field, Val> lobjFields = newHeap.get(lobj);
		lobjFields = lobjFields == null ? Collections.emptyMap() : lobjFields;
		lobjFields = new HashMap<>(lobjFields);
		newHeap.put(lobj, lobjFields);
		lobjFields.put(field, v);

		JmStore newState = new JmStore(this.objects, this.freeObjects, this.env, newHeap);
		return newState;
	}

	public JmStore assign(RefVar lref, Field field, Val v) {
		return assign(eval(lref), field, v);
	}

	public JmStore assign(RefVar lref, IntField field, IntVar var) {
		return assign(eval(lref), field, eval(var));
	}

	public JmStore assign(RefVar lref, Field field, RefVar rref) {
		return assign(eval(lref), field, eval(rref));
	}

	/**
	 * Adds to the set of objects that can be allocated.
	 */
	public void addFreeObjects(HashSet<Obj> objs) {
		freeObjects.addAll(objs);
	}

	/**
	 * Performs garbage collection for removing objects unreachable from the
	 * environment variables.
	 */
	protected JmStore removeGarbage() {
		Set<Obj> reachable = new HashSet<>(env.values().size());
		for (Val v : env.values()) {
			if (v instanceof Obj)
				reachable.add((Obj) v);
		}
		Set<Obj> frontier = new HashSet<>(reachable);
		while (!frontier.isEmpty()) {
			Set<Obj> nextFrontier = new HashSet<>();
			for (Obj obj : frontier) {
				if (obj == Obj.NULL)
					continue;
				Map<Field, Val> objFields = heap.get(obj);
				if (objFields != null) {
					for (Val fieldVal : objFields.values()) {
						if (!(fieldVal instanceof Obj))
							continue;
						Obj succObj = (Obj) fieldVal;
						if (!reachable.contains(succObj)) {
							nextFrontier.add(succObj);
						}
					}
				}
			}
			reachable.addAll(nextFrontier);
			frontier = nextFrontier;
		}
		reachable.remove(Obj.NULL);
		Set<Obj> newObjs = new HashSet<>(reachable);
		Map<Obj, Map<Field, Val>> newHeap = new HashMap<>();
		for (Obj reachhObj : reachable) {
			newHeap.put(reachhObj, this.heap.get(reachhObj));
		}
		JmStore result = new JmStore(newObjs, this.freeObjects, this.env, newHeap);
		return result;
	}

	@Override
	public String toString() {
		ST template = templates.load("Store");
		template.add("vars", env.keySet());
		template.add("refObjs", env.values());
		for (Obj obj : StoreUtils.dfs(this)) {
			ST fieldST = templates.load("Fields");
			Map<Field, Val> fieldMap = heap.get(obj);
			if (fieldMap != null) {
				fieldST.add("obj", obj);
				fieldST.add("fields", fieldMap.keySet());
				fieldST.add("succs", fieldMap.values());
				template.add("heap", fieldST.render());
			}
		}
		return template.render();
	}

	/**
	 * An immutable error store. A description of the error is given by the
	 * description string.
	 * 
	 * @author romanm
	 *
	 */
	public static class JmErrorStore extends JmStore implements ErrorStore {
		public final String description;

		protected JmErrorStore(String description) {
			super();
			this.description = description;
		}

		@Override
		public String message() {
			return description;
		}

		@Override
		public String toString() {
			return "error(\"" + description + "\")";
		}

		@Override
		public boolean equals(Object o) {
			return this == o;
		}

		@Override
		public JmStore clean(Iterable<Var> deadVars) {
			return this;
		}

		@Override
		public Collection<Obj> getObjects() {
			throw new Error("Illegal access to error store!");
		}

		@Override
		public boolean isInitialized(Var var) {
			throw new Error("Illegal access to error store!");
		}

		@Override
		public Val eval(Node code) {
			throw new Error("Illegal access to error store!");
		}

		@Override
		public Val eval(Var var) {
			throw new Error("Illegal access to error store!");
		}

		@Override
		public Obj eval(RefVar var) {
			throw new Error("Illegal access to error store!");
		}

		@Override
		public IntVal eval(IntVar var) {
			throw new Error("Illegal access to error store!");
		}

		@Override
		public Val eval(Obj obj, Field field) {
			throw new Error("Illegal access to error store!");
		}

		@Override
		public Val eval(RefVar var, Field field) {
			throw new Error("Illegal access to error store!");
		}

		@Override
		public Obj eval(Obj obj, RefField field) {
			throw new Error("Illegal access to error store!");
		}

		@Override
		public IntVal eval(Obj obj, IntField field) {
			throw new Error("Illegal access to error store!");
		}

		@Override
		public JmStore assign(Var lvar, Val v) {
			throw new Error("Illegal access to error store!");
		}

		@Override
		public JmStore assign(Var t1, RefVar x) {
			throw new Error("Illegal access to error store!");
		}

		@Override
		public JmStore assign(Obj lobj, Field field, Val v) {
			throw new Error("Illegal access to error store!");
		}

		@Override
		public JmStore assign(RefVar lref, Field field, Val v) {
			throw new Error("Illegal access to error store!");
		}

		@Override
		public JmStore assign(RefVar lref, IntField field, IntVar var) {
			throw new Error("Illegal access to error store!");
		}

		@Override
		public JmStore assign(RefVar lref, Field field, RefVar rref) {
			throw new Error("Illegal access to error store!");
		}

		@Override
		protected JmStore removeGarbage() {
			throw new Error("Illegal access to error store!");
		}
	}
}