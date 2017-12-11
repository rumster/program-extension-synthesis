package heap;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import grammar.*;

/**
 * The operator corresponding to a skip statement.
 * 
 * @author romanm
 */
public class SkipStmt extends BasicStmt {
	public static final SkipStmt v = new SkipStmt();

	public SkipStmt() {
		super(0);
	}

	private SkipStmt(int numOfNonterminals) {
		super(0);
		assert numOfNonterminals == 0;
	}

	@Override
	public List<Node> getArgs() {
		return Collections.emptyList();
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}

	@Override
	public SkipStmt clone(List<Node> args) {
		assert args.isEmpty();
		return this;
	}

	@Override
	public boolean enabled(Store s) {
		return true;
	}

	@Override
	public Collection<Store> apply(Store s) {
		return List.of(s);
	}
	
	@Override
	public String toString() {
		return "skip"; 
	}
}