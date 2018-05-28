package heap_io;

import java.util.ArrayList;
import java.util.Random;

import heap.HeapProblem;
import heap.HeapRunner;
import heap.jsupport.*;

/**
 * A benchmark for filling the data values of a acyclic lists of various
 * lengths.
 * 
 * @author romanm
 */
public class SLLFill extends HeapRunner {
	public static class BenchEnv extends JavaEnv {
		@MethodArg(out = true, readonly = true)
		public SLL head;

		@MethodArg(out = false, readonly = true)
		public int val;

		public SLL t;
	}

	public static void main(String[] args) {
		SLLFill benchmark = new SLLFill();
		benchmark.run();
	}

	@Override
	public HeapProblem genProblem() {
		ArrayList<JavaEnv> inputs = new ArrayList<>();
		Random r = new Random(31);
		for (int i = 1; i < 6; ++i) {
			BenchEnv env = new BenchEnv();
			env.head = SLL.genAcyclicZeroes(i);
			env.val = r.nextInt(100);
			inputs.add(env);
		}
		JavaProblemGenerator problemGen = new JavaProblemGenerator(super.logger);
		HeapProblem problem = problemGen.generate(SLL.class, "fill", inputs);
		return problem;
	}
}