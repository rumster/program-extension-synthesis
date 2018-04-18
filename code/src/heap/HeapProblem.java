package heap;

import java.util.Collection;
import java.util.Map;
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

	@Override
	public boolean match(Store first, Store second) {
		for (Map.Entry<Var, Val> entry : second.getEnvMap().entrySet()) {
			Var var = entry.getKey();
			Val val = entry.getValue();
			if (!first.isInitialized(var) || !first.eval(var).equals(val)) {
				return false;
			}
		}

		for (Obj obj : second.getObjects()) {
			for (Map.Entry<Field, Val> entry : second.geFields(obj).entrySet()) {
				Field field = entry.getKey();
				Val val = entry.getValue();
				if (!first.isInitialized(obj, field) || !first.eval(obj, field).equals(val)) {
					return false;
				}
			}
		}

		// TODO: handle free objects.
		return true;
	}

	@Override
	public Optional<Store> apply(Stmt action, Store state) {
		Collection<Store> succs = BasicHeapTR.applier.apply(state, action);
		if (succs.size() == 1) {
			Store next = succs.iterator().next();
			return Optional.of(next);
		} else {
			return Optional.empty();
		}
	}
}