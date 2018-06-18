package jminor.jsupport;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import bgu.cs.util.ReflectionUtils;
import jminor.Field;
import jminor.IntField;
import jminor.IntVal;
import jminor.IntVar;
import jminor.Obj;
import jminor.RefField;
import jminor.RefType;
import jminor.RefVar;
import jminor.Store;
import jminor.Val;
import jminor.Var;
import jminor.Var.VarRole;

/**
 * Converts a Java state into a {@link Store}. The heap is traversed, starting
 * from a {@link JavaEnv} object whose fields represent the arguments and local
 * variables of a method to be synthesized, the objects reachable from the
 * {@link JavaEnv} object, which are discovered by reflection, constitute the
 * objects of the resulting state.
 * 
 * @author romanm
 */
public class JavaHeapWalker {
	private static final boolean scanStaticFields = false;

	private Map<Class<?>, RefType> clsToRefType = new HashMap<>();
	private Map<Object, Obj> javaObjectToSynthObj = new HashMap<>();
	private Map<Integer, IntVal> intToSynthInt = new HashMap<>();

	private Map<String, Var> nameToVar = new HashMap<>();
	private Map<java.lang.reflect.Field, Field> jfieldToSynthField = new HashMap<>();

	private Map<Var, Val> resultEnv;
	private Map<Obj, Map<Field, Val>> resultHeap;

	private final Logger logger;

	/**
	 * Constructs a heap walker for observing the inputs of the given method whose
	 * arguments are given as fields of the given environment class.
	 * 
	 * @param m
	 *            A Java method signature.
	 * @param envClass
	 *            An environment class whose fields stand for the formal names of
	 *            the given method and of extra local variables.
	 */
	public JavaHeapWalker(Method m, Class<? extends JavaEnv> envClass, Logger logger) {
		this.logger = logger;
		for (java.lang.reflect.Field field : envClass.getFields()) {
			if (field.isAnnotationPresent(MethodArg.class) || field.getName().equals(JavaProblemGenerator.RET_PARAM)
					|| field.getName().equals(JavaProblemGenerator.THIS_PARAM)) {
				MethodArg marg = field.getAnnotation(MethodArg.class);
				boolean isOut = ((marg != null && marg.out()) || field.getName().equals(JavaProblemGenerator.RET_PARAM)
						|| field.getName().equals(JavaProblemGenerator.THIS_PARAM));
				boolean isReadonly = ((marg != null && marg.readonly())
						&& !field.getName().equals(JavaProblemGenerator.RET_PARAM)
						|| field.getName().equals(JavaProblemGenerator.THIS_PARAM));
				createVar(field.getName(), field.getType(), VarRole.ARG, isOut, isReadonly);
			} else {
				createVar(field.getName(), field.getType(), VarRole.TEMP, false, false);
			}
		}
	}

	public Collection<Var> getVars() {
		return nameToVar.values();
	}

	public Collection<RefType> getRefTypes() {
		return clsToRefType.values();
	}

	private Var createVar(String name, Class<?> type, VarRole role, boolean out, boolean readonly) {
		if (ReflectionUtils.isObjectRefType(type)) {
			RefVar var = new RefVar(name, geRefType(type), role, out, readonly);
			nameToVar.put(name, var);
			return var;
		} else if (ReflectionUtils.isIntType(type)) {
			IntVar var = new IntVar(name, role, out, readonly);
			nameToVar.put(name, var);
			return var;
		} else {
			logger.info("Ignoring unknown variable type: " + type.getName() + "!");
			return null;
		}
	}

	public void reset() {
		javaObjectToSynthObj.clear();
	}

	public Store walk(JavaEnv env) throws IllegalArgumentException, IllegalAccessException {
		resultEnv = new HashMap<>();
		resultHeap = new HashMap<>();
		processEnv(env);

		Set<Object> marked = new HashSet<>();

		Set<Object> frontier = new HashSet<>();
		frontier.addAll(javaObjectToSynthObj.keySet());
		while (!frontier.isEmpty()) {
			Iterator<Object> iter = frontier.iterator();
			Object o = iter.next();
			iter.remove();
			if (marked.contains(o)) {
				continue;
			}
			marked.add(o);
			RefType otype = geRefType(o.getClass());
			Obj synthObj = getSynthObj(o);
			Map<Field, Val> synthObjFields = resultHeap.get(synthObj);
			if (synthObjFields == null) {
				synthObjFields = new HashMap<>();
				resultHeap.put(synthObj, synthObjFields);
			}

			for (java.lang.reflect.Field field : o.getClass().getFields()) {
				Class<?> fieldType = field.getType();
				Field synthField = getSynthField(field, otype);
				Object fieldVal = field.get(o);
				if (ReflectionUtils.isObjectRefType(fieldType)) {
					if (fieldVal == null) {
						synthObjFields.put(synthField, Obj.NULL);
					} else {
						frontier.add(fieldVal);
						Obj synthSucc = getSynthObj(fieldVal);
						synthObjFields.put(synthField, synthSucc);
					}
				} else if (ReflectionUtils.isIntType(fieldType)) {
					int intVal = ((Integer) fieldVal).intValue();
					synthObjFields.put(synthField, getSynthInt(intVal));
				} else {
					logger.info("Ignoring field type: " + fieldType.getName() + "!");
				}
			}
		}

		Store result = new Store(resultHeap.keySet(), new HashSet<>(), resultEnv, resultHeap);
		return result;
	}

	private Field getSynthField(java.lang.reflect.Field field, RefType owner) {
		Field result = null;
		Class<?> fieldType = field.getType();
		if (fieldType.isPrimitive()) {
			if (ReflectionUtils.isIntType(fieldType)) {
				result = jfieldToSynthField.get(field);
				if (result == null) {
					result = new IntField(field.getName(), owner, false);
					jfieldToSynthField.put(field, result);
				}
			} else {
				logger.info("Ignoring field type: " + fieldType.getName() + "!");
			}
		} else {
			if (ReflectionUtils.isObjectRefType(fieldType)) {
				RefType dstType = geRefType(fieldType);
				result = jfieldToSynthField.get(field);
				if (result == null) {
					result = new RefField(field.getName(), owner, dstType, false);
					jfieldToSynthField.put(field, result);
				}
			} else {
				// Ignore unwanted types.
			}
		}
		return result;
	}

	private Obj getSynthObj(Object o) {
		if (o == null) {
			return Obj.NULL;
		}

		Obj synthObject = javaObjectToSynthObj.get(o);
		if (synthObject == null) {
			synthObject = new Obj(geRefType(o.getClass()));
			javaObjectToSynthObj.put(o, synthObject);
		}
		return synthObject;
	}

	protected void processEnv(JavaEnv env) throws IllegalArgumentException, IllegalAccessException {
		for (java.lang.reflect.Field field : env.getClass().getFields()) {
			Var var = nameToVar.get(field.getName());
			assert var != null;
			Object val = field.get(env);
			Class<?> fieldType = field.getType();
			if (ReflectionUtils.isObjectRefType(fieldType)) {
				Obj synthObj = getSynthObj(val);
				resultEnv.put(var, synthObj);
			} else if (ReflectionUtils.isIntType(fieldType)) {
				Val synthVal = getSynthInt((Integer) val);
				resultEnv.put(var, synthVal);
			} else {
				logger.info("Ignoring enviornment field of type: " + fieldType.getName() + "!");
			}
		}
	}

	private Val getSynthInt(Integer val) {
		int intVal = val.intValue();
		IntVal result = intToSynthInt.get(val);
		if (result == null) {
			result = new IntVal(intVal);
			intToSynthInt.put(val, result);
		}
		return result;
	}

	/**
	 * Creates and returns a unique {@link RefType} per Java class, populating its
	 * reference and int fields. Array types and synthetic types are ignored.
	 */
	protected RefType geRefType(Class<?> cls) {
		RefType synthType = clsToRefType.get(cls);
		if (synthType == null) {
			synthType = new RefType(cls.getSimpleName());
			clsToRefType.put(cls, synthType);

			for (java.lang.reflect.Field field : cls.getFields()) {
				if (!scanStaticFields && Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				Field synthField = getSynthField(field, synthType);
				if (synthField != null) {
					synthType.add(synthField);
				}
			}
		}
		return synthType;
	}
}