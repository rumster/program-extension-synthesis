package heap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import gp.TR;

/**
 * A transition relation for basic (3-address code) statements.
 * 
 * @author romanm
 */
public class BasicHeapTR implements TR<Store, BasicStmt> {
	public static final BasicHeapTR applier = new BasicHeapTR(
			HeapDomain.fromVarsAndTypes(Collections.emptyList(), Collections.emptyList()));

	protected final HeapDomain domain;

	public BasicHeapTR(HeapDomain domain) {
		this.domain = domain;
	}

	@Override
	public Collection<BasicStmt> enabledActions(Store state) {
		Collection<BasicStmt> result = new ArrayList<>(domain.actions.size());
		for (BasicStmt s : domain.actions) {
			if (s.enabled(state)) {
				result.add(s);
			}
		}
		return result;
	}

	@Override
	public float transitionCost(Store src, BasicStmt action, Store dst) {
		return 1;
	}

	@Override
	public Collection<Store> apply(Store state, BasicStmt stmt) {
		return stmt.apply(state);
	}
}