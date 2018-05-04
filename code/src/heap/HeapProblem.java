package heap;

import gp.Domain;
import gp.SynthesisProblem;

/**
 * A synthesis problem for heap-manipulating programs.
 * 
 * @author romanm
 */
public class HeapProblem extends SynthesisProblem<Store, Stmt, BoolExpr> {
	public final HeapDomain domain;

	public HeapProblem(String name, HeapDomain domain) {
		super(name);
		this.domain = domain;
	}

	@Override
	public String toString() {
		return domain.toString();
	}

	@Override
	public Domain<Store, Stmt, BoolExpr> domain() {
		return domain;
	}
}