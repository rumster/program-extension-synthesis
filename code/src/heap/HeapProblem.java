package heap;

import java.util.Optional;

import gp.Domain;
import gp.SynthesisProblem;

/**
 * A synthesis problem for heap-manipulating programs.
 * 
 * @author romanm
 */
public class HeapProblem extends SynthesisProblem<Store, Stmt, BoolExpr> {
	public final HeapDomain domain;
	public final Optional<Stmt> optProg;

	public HeapProblem(String name, HeapDomain domain, Optional<Stmt> optProg) {
		super(name);
		this.domain = domain;
		this.optProg = optProg;
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