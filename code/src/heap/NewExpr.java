package heap;

import java.util.List;

import grammar.Node;
import grammar.Visitor;

/**
 * The operator corresponding to an object allocation.
 * 
 * @author romanm
 */
public class NewExpr extends Expr {
	public NewExpr(RefType type) {
		super(type);
	}

	public RefType getType() {
		return (RefType) args.get(0);
	}

	protected NewExpr(List<Node> args) {
		super(args);
		assertNumOfArgs(1);
	}

	@Override
	public NewExpr clone(List<Node> args) {
		return new NewExpr(args);
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}
}