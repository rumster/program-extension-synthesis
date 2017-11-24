package heap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.stringtemplate.v4.ST;

import bgu.cs.util.STGLoader;
import treeGrammar.Node;

/**
 * Represents a concrete state of a PWhile program. Uninitialized variables and
 * fields are treated as null.
 * 
 * @author romanm
 */
public class State {
	public static final STGLoader templates = new STGLoader(State.class, "State");

	/**
	 * An immutable top state.
	 */
	public static final State top = new TopState();

	/**
	 * The set of allocated objects.
	 */
	protected final Set<Obj> objects;

	/**
	 * A map assigning variables to values.
	 */
	protected final Map<Var, Val> env;

	/**
	 * A map assigning each object and reference field to the object it references
	 * or null.
	 */
	protected final Map<Obj, Map<Field, Val>> heap;

	public List<Boolean> predicates = new ArrayList<>();

	public static State errorState(String description) {
		return new ErrorState(description);
	}

	/**
	 * Constructs an empty state.
	 */
	public State() {
		this.objects = new HashSet<>();
		this.env = new HashMap<>();
		this.heap = new HashMap<>();
		this.heap.put(Obj.NULL, Collections.emptyMap());
	}

	/**
	 * Constructs a state with the specified set of objects, environment, and heap.
	 * 
	 * @param objects
	 *            The set of allocated objects.
	 * @param env
	 *            An environment mapping variables to fields.
	 * @param heap
	 *            A heap mapping objects and fields to other objects.
	 */
	public State(Set<Obj> objects, Map<Var, Val> env, Map<Obj, Map<Field, Val>> heap) {
		assert objects == null || !objects.contains(Obj.NULL);
		this.objects = objects;
		this.env = env;
		this.heap = heap;
	}

	public Map<Var, Val> getEnvMap() {
		return env;
	}

	public Map<Field, Val> geFields(Obj obj) {
		assert objects.contains(obj);
		Map<Field, Val> result = heap.get(obj);
		return result;
	}

	/**
	 * @return The set of allocated objects.
	 */
	public Collection<Obj> getObjects() {
		return objects;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof State))
			return false;
		if (this == o)
			return true;
		State other = (State) o;
		if (other == top)
			return false;
		if (!objects.equals(other.objects))
			return false;
		if (!env.equals(other.env))
			return false;
		if (!heap.equals(other.heap))
			return false;
		return true;
	}

	public boolean equalHeap(State other) {
		assert objects.equals(other.objects);
		return heap.equals(other.heap);
	}

	public boolean equalEnv(State other) {
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
	 * Returns the state obtained by unmapping the given set of variables and
	 * performing garbage collections.
	 * 
	 * @param deadVars
	 *            A collection of out-of-scope variables.
	 */
	public State clean(Iterable<Var> deadVars) {
		State result = new State(new HashSet<>(this.objects), new HashMap<>(this.env), this.heap);
		for (Var deadVar : deadVars) {
			result.env.remove(deadVar);
		}
		result = result.removeGarbage();
		return result;
	}

	public boolean containsGarbage() {
		int numObjects = objects.size();
		State gfree = removeGarbage();
		int numObjectsAfterGC = gfree.getObjects().size();
		return numObjects != numObjectsAfterGC;
	}

	public boolean isUndefined(Var var) {
		return !env.containsKey(var);
	}

	/**
	 * Evaluates an expression in this state returning its value or null if the
	 * expression is illegal (e.g., a null dereference or an access to an
	 * uninitialized variable).
	 */
	public Val eval(Node code) {
		if (code instanceof Var) {
			Var var = (Var) code;
			return eval(var);
		} else if (code instanceof OpDeref) {
			OpDeref deref = (OpDeref) code;
			Node lhs = deref.getLhs();
			Val lval = eval(lhs);
			if (lval == null || lval == Obj.NULL || lval == Obj.TOP)
				return null;
			Obj lobj = (Obj) lval;
			Field field = deref.getField();
			return eval(lobj, field);
		} else {
			assert false : "Unexpected expression " + code + "!";
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
	public Int eval(IntVar var) {
		Int result = (Int) env.get(var);
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
		return objFields.get(field);
	}

	public Val eval(RefVar var, Field field) {
		Obj obj = eval(var);
		if (obj == null)
			return Val.TOP;
		else
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

	public Int eval(Obj obj, IntField field) {
		assert obj != null;
		Map<Field, Val> objFields = heap.get(obj);
		return (Int) objFields.get(field);
	}

	/**
	 * Creates the state resulting from allocating a new object of the predefined
	 * type and assigning it to the given variable.
	 */
	public State assignNewObj(RefVar lvar) {
		Set<Obj> newStateObjects = new HashSet<>(this.objects);
		Map<Var, Val> newEnv = new HashMap<>(this.env);
		Obj newObj = new Obj(lvar.type);
		newStateObjects.add(newObj);
		newEnv.put(lvar, newObj);

		State newState = new State(newStateObjects, newEnv, updateHeap(this.heap, lvar, newObj));
		return newState;
	}

	/**
	 * @param lvar
	 * @param newObj
	 * @returns the heap after assigning newObj to lvar
	 */
	static protected Map<Obj, Map<Field, Val>> updateHeap(Map<Obj, Map<Field, Val>> oldHeap, RefVar lvar, Obj newObj) {
		Map<Obj, Map<Field, Val>> newHeap = new HashMap<>(oldHeap);
		Map<Field, Val> newObjVals = new HashMap<>(lvar.type.fields.size());
		for (Field field : lvar.type.fields) {
			newObjVals.put(field, field.getDefaultVal());
		}
		newHeap.put(newObj, newObjVals);
		return newHeap;
	}

	public State assign(Var lvar, Val v) {
		assert lvar != null && v != null && typecheck(lvar, v);
		Map<Var, Val> newEnv = new HashMap<>(this.env);
		newEnv.put(lvar, v);
		State newState = new State(this.objects, newEnv, this.heap);
		return newState;
	}

	public State assign(Var t1, RefVar x) {
		return assign(t1, eval(x));
	}

	public State assign(Obj lobj, Field field, Val v) {
		assert lobj != null && field != null && v != null;
		assert typecheck(field, v);
		assert typecheck(field, lobj);

		Map<Obj, Map<Field, Val>> newHeap = new HashMap<>(heap);
		Map<Field, Val> lobjFields = newHeap.get(lobj);
		lobjFields = new HashMap<>(lobjFields);
		newHeap.put(lobj, lobjFields);
		lobjFields.put(field, v);

		State newState = new State(this.objects, this.env, newHeap);
		return newState;
	}

	public State assign(RefVar lref, Field field, Val v) {
		return assign(eval(lref), field, v);
	}

	public State assign(RefVar lref, IntField field, IntVar var) {
		return assign(eval(lref), field, eval(var));
	}

	public State assign(RefVar lref, Field field, RefVar rref) {
		return assign(eval(lref), field, eval(rref));
	}

	/**
	 * Performs garbage collection for removing objects unreachable from the
	 * environment variables.
	 */
	protected State removeGarbage() {
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
				for (Val fieldVal : objFields.values()) {
					if (!(fieldVal instanceof Obj))
						continue;
					Obj succObj = (Obj) fieldVal;
					if (!reachable.contains(succObj)) {
						nextFrontier.add(succObj);
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
		State result = new State(newObjs, this.env, newHeap);
		return result;
	}

	@Override
	public String toString() {
		ST template = templates.load("State");
		template.add("vars", env.keySet());
		template.add("refObjs", env.values());
		for (Obj obj : StateOps.dfs(this)) {
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

	public boolean typecheck(Var var, Val v) {
		return (var instanceof IntVar && v instanceof Int)
				|| (var instanceof RefVar && (v == null || v instanceof Obj));
	}

	public boolean typecheck(Field field, Val v) {
		return (field instanceof IntField && v instanceof Int)
				|| (field instanceof RefField && (v == null || v instanceof Obj));
	}

	public boolean typecheck(Field field, Obj o) {
		return (field.srcType.equals(o.type));
	}

	/**
	 * The base class of values.
	 * 
	 * @author romanm
	 */
	public static abstract class Val {
		public static final Val TOP = new Val() {
		};

		public String getName() {
			throw new Error("shouldnt be called");
		};
	}

	public static class Symbolic extends Val {
	}

	/**
	 * An integer value.
	 * 
	 * @author romanm
	 */
	public static class Int extends Val {
		public static final RefType type = new RefType("Int");
		public static final IntField field = new IntField("val", type);
		public static final Int ZERO = new Int(0);

		public final int num;

		public Int(int num) {
			this.num = num;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Int))
				return false;
			Int other = (Int) o;
			return this.num == other.num;
		}

		@Override
		public String toString() {
			return "" + num;
		}

		public String getName() {
			return "i" + num;
		}

		public Int add(Int o) {
			return new Int(num + o.num);
		}
	}

	/**
	 * A heap object.
	 * 
	 * @author romanm
	 *
	 */
	public static class Obj extends Val {
		public static final Obj NULL = new Obj(new RefType("nulltype")) {
			@Override
			public String getName() {
				return "NULL";
			}

			@Override
			public String toString() {
				return "NULL";
			}
		};

		public final RefType type;
		public final int count;

		private static int counter = 0;

		public Obj(RefType type) {
			this.type = type;
			this.count = counter;
			++counter;
		}

		@Override
		public String toString() {
			return type + "(" + count + ")";
		}

		public String getName() {
			return "o" + count;
		}
	}

	/**
	 * An immutable state - any attempt to modify the state results in a raised
	 * exception.
	 * 
	 * @author romanm
	 */
	protected static class ImmutableState extends State {
		public ImmutableState() {
			super();
		}

		public ImmutableState(Set<Obj> objects, Map<Var, Val> env, Map<Obj, Map<Field, Val>> heap) {
			super(objects, env, heap);
		}

		public State assignNewObj(RefVar lvar) {
			throw new Error("Attempt to modify an immutable state " + getClass().getName());
		}

		public Collection<Obj> getObjects() {
			if (objects == null) {
				throw new Error("Attempt to access the objects from a dummy state!");
			} else {
				return super.getObjects();
			}
		}

		public Obj eval(RefVar var) {
			if (env == null) {
				throw new Error("Attempt to access the environment of a dummy state!");
			} else {
				return super.eval(var);
			}
		}

		public Obj eval(Obj obj, RefField field) {
			if (heap == null) {
				throw new Error("Attempt to access the heap of a dummy state!");
			} else {
				return super.eval(obj, field);
			}
		}
	}

	/**
	 * A marker interface for states which have no outgoing transitions.
	 * 
	 * @author romanm
	 */
	public static interface TerminalState {
	}

	/**
	 * An immutable error state. A description of the error is given by the
	 * description string.
	 * 
	 * @author romanm
	 *
	 */
	public static class ErrorState extends ImmutableState implements TerminalState {
		public final String description;

		protected ErrorState(String description) {
			super(null, null, null);
			this.description = description;
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
		public State clean(Iterable<Var> deadVars) {
			return this;
		}
	}

	/**
	 * An immutable top state.
	 * 
	 * @author romanm
	 */
	public static class TopState extends ImmutableState implements TerminalState {
		protected TopState() {
			super(null, null, null);
		}

		@Override
		public boolean equals(Object o) {
			return this == o;
		}

		@Override
		public State clean(Iterable<Var> deadVars) {
			return this;
		}
	}
}