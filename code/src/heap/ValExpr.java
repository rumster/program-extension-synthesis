package heap;

import java.util.List;

import grammar.Node;
import grammar.Visitor;

/**
 * A constant value expression.
 * 
 * @author romanm
 */
public class ValExpr extends Expr {
	public ValExpr(Val v) {
		super(v);
	}

	public Val getVal() {
		return (Val) args.get(0);
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}

	protected ValExpr(List<Node> args) {
		super(args);
		assertNumOfArgs(1);
	}

	@Override
	public ValExpr clone(List<Node> args) {
		assert args.size() == 1;
		return new ValExpr(args);
	}
}