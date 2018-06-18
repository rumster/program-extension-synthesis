package jminor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import pexyn.planning.TR;

/**
 * A transition relation for basic (3-address code) statements.
 * 
 * @author romanm
 */
public class BasicJminorTR implements TR<JmStore, Stmt> {
	public static final BasicJminorTR applier = new BasicJminorTR(
			JminorDomain.fromVarsAndTypes(Collections.emptyList(), Collections.emptyList()));

	protected final JminorDomain domain;

	public BasicJminorTR(JminorDomain domain) {
		this.domain = domain;
	}

	@Override
	public Collection<Stmt> enabledActions(JmStore store) {
		Collection<Stmt> result = new ArrayList<>(domain.stmts.size());
		for (var stmt : domain.stmts) {
			if (stmt.enabled(store)) {
				result.add(stmt);
			}
		}
		return result;
	}

	@Override
	public float transitionCost(JmStore src, Stmt action, JmStore dst) {
		return 1;
	}

	@Override
	public Collection<JmStore> apply(JmStore store, Stmt stmt) {
		JmStore result = PWhileInterpreter.v.run(stmt, store, PWhileInterpreter.v.guessMaxSteps(stmt, store)).get();
		return List.of(result);
	}
}