package jminor;

import java.util.List;

import bgu.cs.util.treeGrammar.Node;
import bgu.cs.util.treeGrammar.Visitor;

/**
 * A skip statement.
 * 
 * @author romanm
 */
public class SkipStmt extends Stmt {
	public static final SkipStmt v = new SkipStmt();

	@Override
	public void accept(Visitor v) {
		JminorVisitor whileVisitor = (JminorVisitor) v;
		whileVisitor.visit(this);
	}

	@Override
	public SkipStmt clone(List<Node> args) {
		assertNumOfArgs(0);
		return this;
	}

	@Override
	public String toString() {
		return "skip";
	}
}