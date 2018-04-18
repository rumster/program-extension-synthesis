package heap;

import java.util.List;

import grammar.Node;
import grammar.Visitor;

/**
 * The operator corresponding to a conjunctive Boolean connective.
 * 
 * @author romanm
 */
public class AndExpr extends BoolExpr {
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
		super(lhs, rhs);
		args.add(lhs);
		args.add(rhs);
	}

	protected AndExpr(List<Node> args) {
		super(args);
		assertNumOfArgs(2);
	}

	@Override
	public AndExpr clone(List<Node> args) {
		return new AndExpr(args);
	}
}