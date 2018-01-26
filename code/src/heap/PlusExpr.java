package heap;

import java.util.ArrayList;
import java.util.List;

import grammar.*;

/**
 * The operator corresponding to an addition of two integer values.
 * 
 * @author romanm
 */
public class PlusExpr extends ArithExpr {
	protected List<Node> args = new ArrayList<>(2);

	@Override
	public List<Node> getArgs() {
		return args;
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

	/**
	 * Constructs the right-hand side of an equality comparison.
	 */
	public PlusExpr(Node lhs, Node rhs) {
		args.add(lhs);
		args.add(rhs);
	}

	protected PlusExpr(List<Node> args) {
		assert args.size() == 2 : "Illegal number of arguments for " + getClass().getSimpleName() + ": " + args.size()
				+ "!";
		this.args = args;
	}

	@Override
	public PlusExpr clone(List<Node> args) {
		return new PlusExpr(args);
	}

	@Override
	public IntVal evaluate(Store store) {
		IntVal lval = (IntVal) store.eval(getLhs());
		IntVal rval = (IntVal) store.eval(getRhs());
		return new IntVal(lval.num + rval.num);
	}
}