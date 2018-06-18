package jminor;

import java.util.List;

import grammar.Node;
import grammar.Visitor;

/**
 * A parallel assignment.
 * 
 * @author romanm
 */
public class ParallelAssign extends Stmt {
	public ParallelAssign(ExprList lvals, ExprList rvals) {
		super(lvals, rvals);
		assert lvals.size() == rvals.size();
	}

	public ParallelAssign(List<Node> args) {
		super(args);
		assertNumOfArgs(2);
	}

	public Node lvals() {
		return args.get(0);
	}

	public Node rvals() {
		return args.get(1);
	}

	@Override
	public void accept(Visitor v) {
		JminorVisitor whileVisitor = (JminorVisitor) v;
		whileVisitor.visit(this);
	}

	@Override
	public Node clone(List<Node> args) {
		return new ParallelAssign(args);
	}
}
