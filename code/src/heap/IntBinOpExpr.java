package heap;

import java.util.List;

import grammar.Node;
import grammar.Visitor;

/**
 * A binary operation over two integer values.
 * 
 * @author romanm
 */
public class IntBinOpExpr extends Expr {
	public final IntBinOp op;
	
	public IntBinOpExpr(IntBinOp op, Node lhs, Node rhs) {
		super(lhs, rhs);
		this.op = op;
	}

	public Node getLhs() {
		return args.get(0);
	}

	public Node getRhs() {
		return args.get(1);
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}

	protected IntBinOpExpr(IntBinOp op, List<Node> args) {
		super(args);
		this.op = op;
		assertNumOfArgs(2);
	}

	@Override
	public IntBinOpExpr clone(List<Node> args) {
		return new IntBinOpExpr(op, args);
	}
}