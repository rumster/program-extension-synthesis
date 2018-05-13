package heap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import gp.planning.TR;

/**
 * A transition relation for basic (3-address code) statements.
 * 
 * @author romanm
 */
public class BasicHeapTR implements TR<Store, Stmt> {
	public static final BasicHeapTR applier = new BasicHeapTR(
			HeapDomain.fromVarsAndTypes(Collections.emptyList(), Collections.emptyList()));

	protected final HeapDomain domain;

	public BasicHeapTR(HeapDomain domain) {
		this.domain = domain;
	}

	@Override
	public Collection<Stmt> enabledActions(Store store) {
		Collection<Stmt> result = new ArrayList<>(domain.stmts.size());
		for (var stmt : domain.stmts) {
			if (stmt.enabled(store)) {
				result.add(stmt);
			}
		}
		return result;
	}

	@Override
	public float transitionCost(Store src, Stmt action, Store dst) {
		return 1;
	}

	@Override
	public Collection<Store> apply(Store store, Stmt stmt) {
		Store result = PWhileInterpreter.v.run(stmt, store, PWhileInterpreter.v.guessMaxSteps(stmt, store)).get();
		return List.of(result);
	}
}