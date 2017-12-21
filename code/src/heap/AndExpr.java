package heap;

import java.util.ArrayList;
import java.util.List;

import grammar.*;

/**
 * The operator corresponding to a conjunctive Boolean connective.
 * 
 * @author romanm
 */
public class AndExpr extends Node implements Condition {
	protected List<Node> args = new ArrayList<>(2);

	protected AndExpr(int numOfNonterminals) {
		super(numOfNonterminals);
	}

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
	 * Constructs the right-hand side of the conjunction rule.
	 */
	public AndExpr(Node lhs, Node rhs) {
		super(lhs.numOfNonterminals + rhs.numOfNonterminals);
		args.add(lhs);
		args.add(rhs);
	}

	protected AndExpr(List<Node> args) {
		super(countNonterminals(args));
		assert args.size() == 2 : "Illegal number of arguments for " + getClass().getSimpleName() + ": " + args.size()
				+ "!";
		this.args = args;
	}

	@Override
	public AndExpr clone(List<Node> args) {
		return new AndExpr(args);
	}

	@Override
	public boolean holds(Store s) {
		for (Node arg: args) {
			Condition c = (Condition) arg;
			if (!c.holds(s)) {
				return false;
			}
		}
		return true;
	}
}