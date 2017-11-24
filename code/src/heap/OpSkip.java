package heap;

import java.util.Collections;
import java.util.List;

import treeGrammar.*;

/**
 * The operator corresponding to a skip statement.
 * 
 * @author romanm
 */
public class OpSkip extends InternalNode {
	public static final OpSkip v = new OpSkip();

	public OpSkip() {
		super(0);
	}

	private OpSkip(int numOfNonterminals) {
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
	public OpSkip clone(List<Node> args) {
		assert args.isEmpty();
		return this;
	}
}