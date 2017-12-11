package heap;

import java.util.ArrayList;
import java.util.List;

import grammar.*;

/**
 * The operator corresponding to an object allocation.
 * 
 * @author romanm
 */
public class NewExpr extends Node {
	protected List<Node> args = new ArrayList<>(2);

	public NewExpr(RefType type) {
		super(1);
		args.add(type);
	}

	protected NewExpr(int numOfNonterminals) {
		super(numOfNonterminals);
	}

	@Override
	public List<Node> getArgs() {
		return args;
	}

	public RefType getType() {
		return (RefType) args.get(0);
	}

	protected NewExpr(List<Node> args) {
		super(countNonterminals(args));
		assert args.size() == 1 : "Illegal number of arguments for " + getClass().getSimpleName() + ": " + args.size()
				+ "!";
		this.args = args;
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