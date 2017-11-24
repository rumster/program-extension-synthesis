package heap;

import treeGrammar.Terminal;
import treeGrammar.Visitor;

/**
 * A terminal representing the null value.
 * 
 * @author romanm
 */
public class Null extends Terminal {
	public static final Null v = new Null();
	
	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}

	private Null() {
	}
}