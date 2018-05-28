package heap_io;

import java.util.ArrayList;

import heap.HeapProblem;
import heap.HeapRunner;
import heap.jsupport.*;

/**
 * A benchmark for list reversal on acyclic lists of various lengths.
 * 
 * @author romanm
 */
public class SLLReverse extends HeapRunner {
	public static class BenchEnv extends JavaEnv {
		@MethodArg(out = false)
		public SLL head;
		public SLL ret;
		public SLL t1;
		public SLL t2;
	}

	public static void main(String[] args) {
		SLLReverse benchmark = new SLLReverse();
		benchmark.run();
	}

	@Override
	public HeapProblem genProblem() {
		ArrayList<JavaEnv> inputs = new ArrayList<>();
		for (int i = 3; i >= 1; --i) {/// 100
			BenchEnv env = new BenchEnv();
			env.head = SLL.genRandomAcyclic(i);
			inputs.add(env);
		}
		JavaProblemGenerator problemGen = new JavaProblemGenerator(super.logger);
		HeapProblem problem = problemGen.generate(SLL.class, "reverse", inputs);
		return problem;
	}
}