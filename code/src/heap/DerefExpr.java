package heap;

import java.util.List;

import grammar.Node;
import grammar.Visitor;

/**
 * The operator corresponding to an object field access.
 * 
 * @author romanm
 */
public class DerefExpr extends Expr {
	public DerefExpr(Node base, Field field) {
		super(base, field);
	}

	public int depth() {
		Node lhs = getLhs();
		if (lhs instanceof DerefExpr) {
			return 1 + ((DerefExpr) lhs).depth();
		} else {
			return 1;
		}
	}

	public Type dstType() {
		Field f = getField();
		return f.dstType;
	}

	public Node getLhs() {
		return args.get(0);
	}

	public Field getField() {
		return (Field) args.get(1);
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}

	protected DerefExpr(List<Node> args) {
		super(args);
		assertNumOfArgs(2);
	}

	@Override
	public DerefExpr clone(List<Node> args) {
		assert args.size() == 2;
		return new DerefExpr(args);
	}
}