package heap;

import java.util.ArrayList;

import dataStructures.SLL;
import heap.HeapSynthesisProblem;
import heap.jsupport.*;

/**
 * A benchmark for 'SLL.findMax'.
 * 
 * @author romanm
 */
public class SLLFindMax extends HeapRunner {
	public static class BenchEnv extends JavaEnv {
		@MethodArg(out = true)
		public SLL head;

		@MethodArg(out = true)
		public SLL ret;
	}

	public static void main(String[] args) {
		SLLFindMax benchmark = new SLLFindMax();
		benchmark.run(args);
	}

	@Override
	public HeapSynthesisProblem genProblem() {
		ArrayList<JavaEnv> inputs = new ArrayList<>();
		for (int i = 1; i < 100; ++i) {
			BenchEnv env = new BenchEnv();
			env.head = SLL.genRandomAcyclic(i);
			env.ret = null;
			inputs.add(env);
		}
		JavaProblemGenerator problemGen = new JavaProblemGenerator(super.logger);
		HeapSynthesisProblem problem = problemGen.generate(SLL.class, "findMax", inputs);
		return problem;
	}
}