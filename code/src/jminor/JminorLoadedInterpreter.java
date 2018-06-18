package jminor;

import java.util.Optional;

import pexyn.LoadedInterpreter;
import pexyn.Plan;

/**
 * A loaded interpreter for a heap-manipulating program.
 * 
 * @author romanm
 */
public class JminorLoadedInterpreter implements LoadedInterpreter<JmStore, Stmt, BoolExpr> {
	private final Stmt prog;

	public JminorLoadedInterpreter(Stmt prog) {
		this.prog = prog;
	}

	@Override
	public Optional<JmStore> run(JmStore input, int maxSteps) {
		return JminorInterpreter.v.run(prog, input, maxSteps);
	}

	@Override
	public Optional<Plan<JmStore, Stmt>> genTrace(JmStore input, int maxSteps) {
		return JminorInterpreter.v.genTrace(prog, input, maxSteps);
	}
}
