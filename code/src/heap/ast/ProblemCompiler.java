package heap.ast;

import heap.HeapProblem;

public class ProblemCompiler {
	protected final ASTProblem root;

	public ProblemCompiler(ASTProblem root) {
		this.root = root;
	}

	public HeapProblem compile() {
		throw new UnsupportedOperationException("unimplemented!");
	}
}