package heap.jsupport;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import bgu.cs.util.ReflectionUtils;
import heap.*;
import heap.Store.Obj;

/**
 * Generates a {@link HeapSynthesisProblem} from a Java {@link Method} and an
 * object where the fields correspond to the method arguments.
 * 
 * @author romanm
 */
public class JavaProblemGenerator {
	public static final String RET_PARAM = "ret";
	public static final String THIS_PARAM = "self";

	private JavaHeapWalker heapWalker;
	private final Logger logger;

	public JavaProblemGenerator(Logger logger) {
		this.logger = logger;
	}

	public HeapSynthesisProblem generate(Class<?> owner, String methodName, List<JavaEnv> ig) {
		return generate(ReflectionUtils.getMethodByName(owner, methodName), ig);
	}

	public HeapSynthesisProblem generate(Method m, List<JavaEnv> inputs) {
		if (inputs.size() == 0)
			return null;
		Class<? extends JavaEnv> envType = inputs.get(0).getClass();
		heapWalker = new JavaHeapWalker(m, envType, logger);
		HeapDomain domain = HeapDomain.fromVarsAndTypes(heapWalker.getVars(), heapWalker.getRefTypes());
		HeapSynthesisProblem result = new HeapSynthesisProblem(m.getName(), domain);

		for (JavaEnv input : inputs) {
			try {
				heapWalker.reset();
				Store inputState = heapWalker.walk(input);

				Field[] envFields = envType.getFields();
				int numOfParameters = m.getParameters().length;
				Object[] actualArgs = new Object[numOfParameters];
				for (int i = 0; i < numOfParameters; ++i) {
					actualArgs[i] = input.getParam(envFields[i].getName());
				}
				Object resultObj = null;
				try {
					resultObj = m.invoke(input.getParam(THIS_PARAM), actualArgs);
				} catch (IllegalArgumentException e) {
					logger.severe(
							"Check that the seqeucen fields in the environment object matches those of the method!");
					throw e;
				}
				JavaEnv output = input;
				if (ReflectionUtils.fieldExists(output, RET_PARAM))
					output.setParam(RET_PARAM, resultObj);
				Store outputState = heapWalker.walk(output);
				HashSet<Obj> freeObjects = new HashSet<>();
				freeObjects.addAll(outputState.getObjects());
				freeObjects.removeAll(inputState.getObjects());
				inputState.addFreeObjects(freeObjects);
				
				//outputState = outputState.clean(heapWalker.getDeadOutVars());
				result.addExample(inputState, outputState);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
				return null;
			}
		}

		return result;
	}
}