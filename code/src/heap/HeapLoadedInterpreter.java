package heap;

import java.util.Optional;

import gp.LoadedInterpreter;
import gp.Plan;

public class HeapLoadedInterpreter implements LoadedInterpreter<Store, Stmt, BoolExpr> {
	private final Stmt prog;

	public HeapLoadedInterpreter(Stmt prog) {
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
