package jminor;

import java.util.List;

import grammar.Node;
import grammar.Visitor;

/**
 * A looping statement.
 * 
 * @author romanm
 */
public class WhileStmt extends Stmt {
	public WhileStmt(Node condNode, Node bodyNode) {
		super(condNode, bodyNode);
	}

	protected WhileStmt(int numOfNonterminals) {
		super(numOfNonterminals);
	}

	public Node getCond() {
		return args.get(0);
	}

	public Node getBody() {
		return args.get(1);
	}

	protected WhileStmt(List<Node> args) {
		super(args);
		assertNumOfArgs(2);
	}

	@Override
	public WhileStmt clone(List<Node> args) {
		return new WhileStmt(args);
	}

	@Override
	public void accept(Visitor v) {
		JminorVisitor whileVisitor = (JminorVisitor) v;
		whileVisitor.visit(this);
	}
}