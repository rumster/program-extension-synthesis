package heap_io;

import java.util.ArrayList;

import heap.HeapProblem;
import heap.HeapRunner;
import heap.jsupport.*;

/**
 * A benchmark for finding the first cell with the given integer value. lengths.
 * 
 * @author romanm
 */
public class SLLFind extends HeapRunner {
	public static class BenchEnv extends JavaEnv {
		@MethodArg(out = true, readonly = true)
		public SLL head;

		@MethodArg(out = false, readonly = true)
		public int val;

		@MethodArg(out = true)
		public SLL ret;
	}

	public static void main(String[] args) {
		SLLFind benchmark = new SLLFind();
		benchmark.run();
	}

	@Override
	public HeapProblem genProblem() {
		ArrayList<JavaEnv> inputs = new ArrayList<>();
		for (int i = 2; i < 100; ++i) {
			BenchEnv env = new BenchEnv();
			env.head = SLL.genRandomAcyclic(i);
			SLL t = env.head;
			if (i % 2 == 0) {
				for (int j = 0; j < i / 2; ++j) {
					t = t.n;
				}
			}
			env.val = t.d;
			inputs.add(env);
		}
		JavaProblemGenerator problemGen = new JavaProblemGenerator(super.logger);
		HeapProblem problem = problemGen.generate(SLL.class, "find", inputs);
		return problem;
	}
}