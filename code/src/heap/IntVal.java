package heap;

import treeGrammar.Terminal;
import treeGrammar.Visitor;

/**
 * A terminal representing an integer value.
 * 
 * @author romanm
 */
public class IntVal extends Terminal {
	public final int val;

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}

	public IntVal(int val) {
		this.val = val;
	}
}