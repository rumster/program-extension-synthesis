package jminor;

import java.util.List;

import bgu.cs.util.treeGrammar.Node;
import bgu.cs.util.treeGrammar.Visitor;

/**
 * The base class of assignment statements.
 * 
 * @author romanm
 */
public class AssignStmt extends Stmt {
	public AssignStmt(Node lhs, Node rhs) {
		super(lhs, rhs);
		assert !(lhs instanceof Var);
		assert !(rhs instanceof Var);
	}

	public Node getLhs() {
		return args.get(0);
	}

	public Node getRhs() {
		return args.get(1);
	}

	@Override
	public void accept(Visitor v) {
		JminorVisitor whileVisitor = (JminorVisitor) v;
		whileVisitor.visit(this);
	}

	protected AssignStmt(List<Node> args) {
		super(args);
		assertNumOfArgs(2);
	}

	@Override
	public Node clone(List<Node> args) {
		assertNumOfArgs(2);
		return new AssignStmt(args);
	}
}