package jminor;

import java.util.Optional;

import pexyn.LoadedInterpreter;
import pexyn.Plan;

/**
 * A loaded interpreter for a heap-manipulating program.
 * 
 * @author romanm
 */
public class JminorLoadedInterpreter implements LoadedInterpreter<Store, Stmt, BoolExpr> {
	private final Stmt prog;

	public JminorLoadedInterpreter(Stmt prog) {
		this.prog = prog;
	}

	@Override
	public Optional<Store> run(Store input, int maxSteps) {
		return PWhileInterpreter.v.run(prog, input, maxSteps);
	}

	@Override
	public Optional<Plan<Store, Stmt>> genTrace(Store input, int maxSteps) {
		return PWhileInterpreter.v.genTrace(prog, input, maxSteps);
	}
}
