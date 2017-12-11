package heap;

import gp.InputOutputProblem;

/**
 * A synthesis problem for heap-manipulating programs.
 * 
 * @author romanm
 */
public class HeapSynthesisProblem extends InputOutputProblem<Store, BasicStmt, Condition> {
	public final HeapDomain domain;
	
	public HeapSynthesisProblem(String name, HeapDomain domain) {
		super(name);
		this.domain = domain;
	}

	@Override
	public String toString() {
		return domain.toString();
	}
}