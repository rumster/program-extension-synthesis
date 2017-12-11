package heap;

import java.util.ArrayList;
import java.util.List;

import grammar.*;

/**
 * The operator corresponding to disjunctive Boolean connective.
 * 
 * @author romanm
 */
public class OrExpr extends Node {
	protected List<Node> args = new ArrayList<>(2);

	protected OrExpr(int numOfNonterminals) {
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
	 * Constructs the right-hand side of the disjunction rule.
	 */
	public OrExpr(Node lhs, Node rhs) {
		super(lhs.numOfNonterminals + rhs.numOfNonterminals);
		args.add(lhs);
		args.add(rhs);
	}

	protected OrExpr(List<Node> args) {
		super(countNonterminals(args));
		assert args.size() == 2 : "Illegal number of arguments for " + getClass().getSimpleName() + ": " + args.size()
				+ "!";
		this.args = args;
	}

	@Override
	public OrExpr clone(List<Node> args) {
		return new OrExpr(args);
	}
}