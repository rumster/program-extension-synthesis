package jminor;

import java.util.List;

import bgu.cs.util.treeGrammar.Node;
import bgu.cs.util.treeGrammar.Visitor;

/**
 * A return statement.
 * 
 * @author romanm
 */
public class RetStmt extends Stmt {
	public static final RetStmt v = new RetStmt();
	
	@Override
	public RetStmt clone(List<Node> args) {
		return v;
	}

	@Override
	public void accept(Visitor v) {
		JminorVisitor whileVisitor = (JminorVisitor) v;
		whileVisitor.visit(this);
	}
	
	@Override
	public boolean equals(Object o) {
		return o == this;
	}

	private RetStmt() {
		super();
	}
}