package heap;

import java.util.List;

import grammar.Node;
import grammar.Visitor;

/**
 * A return statement.
 * 
 * @author romanm
 */
public class RetStmt extends Stmt {
	public static final RetStmt v = new RetStmt();
	
	private RetStmt() {
		super();
	}

	@Override
	public RetStmt clone(List<Node> args) {
		return v;
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}
}