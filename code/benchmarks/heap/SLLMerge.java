package heap;

import java.util.ArrayList;

import dataStructures.SLL;
import heap.HeapSynthesisProblem;
import heap.jsupport.*;

/**
 * A benchmark for merging to sorted acyclic lists of various lengths.
 * 
 * @author romanm
 */
public class SLLMerge extends HeapRunner {
	public static class BenchEnv extends JavaEnv {
		@MethodArg(out = false)
		public SLL first;
		@MethodArg(out = false)
		public SLL second;
		public SLL ret;
		public SLL t1;
		public SLL t2;
	}

	public static void main(String[] args) {
		SLLMerge benchmark = new SLLMerge();
		benchmark.run(args);
	}

	@Override
	public HeapSynthesisProblem genProblem() {
		ArrayList<JavaEnv> inputs = new ArrayList<>();
		for (int i = 14; i < 16; ++i) {
			BenchEnv env = new BenchEnv();
			env.first = SLL.genAcyclicSorted(i);
			env.second = SLL.genAcyclicSorted(i);
			inputs.add(env);
		}
		JavaProblemGenerator problemGen = new JavaProblemGenerator(super.logger);
		HeapSynthesisProblem problem = problemGen.generate(SLL.class, "merge", inputs);
		return problem;
	}
}