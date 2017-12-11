package heap;

import grammar.Token;
import grammar.Visitor;

/**
 * A terminal representing the null value.
 * 
 * @author romanm
 */
public class NullExpr extends Token {
	public static final NullExpr v = new NullExpr();
	
	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}

	private NullExpr() {
	}
}