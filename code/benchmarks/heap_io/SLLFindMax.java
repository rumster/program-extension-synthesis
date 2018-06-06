package heap_io;

import java.util.ArrayList;

import heap.HeapProblem;
import heap.Main;
import heap.jsupport.*;

/**
 * A benchmark for 'SLL.findMax'.
 * 
 * @author romanm
 */
public class SLLFindMax extends Main {
	public static class BenchEnv extends JavaEnv {
		@MethodArg(out = true)
		public SLL head;

		@MethodArg(out = true)
		public SLL ret;
	}

	public SLLFindMax(String name) {
		super(name);
	}

	public static void main(String[] args) {
		SLLFindMax benchmark = new SLLFindMax("findMax");
		benchmark.run();
	}

	@Override
	public HeapProblem genProblem() {
		ArrayList<JavaEnv> inputs = new ArrayList<>();
		for (int i = 1; i < 100; ++i) {
			BenchEnv env = new BenchEnv();
			env.head = SLL.genRandomAcyclic(i);
			env.ret = null;
			inputs.add(env);
		}
		JavaProblemGenerator problemGen = new JavaProblemGenerator(super.logger);
		HeapProblem problem = problemGen.generate(SLL.class, "findMax", inputs);
		return problem;
	}
}