package heap;

import java.util.ArrayList;
import java.util.List;

import grammar.*;

/**
 * The operator corresponding to a Boolean negation.
 * 
 * TODO: optimize by using a single field for the sub-node.
 * 
 * @author romanm
 */
public class NotExpr extends Node implements Condition {
	protected List<Node> args = new ArrayList<>(1);

	protected NotExpr(int numOfNonterminals) {
		super(numOfNonterminals);
	}

	@Override
	public List<Node> getArgs() {
		return args;
	}

	public Node getSub() {
		return args.get(0);
	}

	/**
	 * Constructs the right-hand side of the conjunction rule.
	 */
	public NotExpr(Node sub) {
		super(sub.numOfNonterminals);
		args.add(sub);
	}

	protected NotExpr(List<Node> args) {
		super(countNonterminals(args));
		assert args.size() == 1 : "Illegal number of arguments for " + getClass().getSimpleName() + ": " + args.size()
				+ "!";
		this.args = args;
	}

	@Override
	public NotExpr clone(List<Node> args) {
		return new NotExpr(args);
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}

	@Override
	public boolean holds(Store s) {
		return PWhileInterpreter.v.test(this, s);
	}
}