package jminor;

import java.util.List;

import bgu.cs.util.treeGrammar.Node;
import bgu.cs.util.treeGrammar.Visitor;

/**
 * A list of expressions.
 * 
 * @author romanm
 */
public class ExprList extends Expr {
	public ExprList(List<Node> args) {
		super(args);
	}

	public int size() {
		return args.size();
	}

	public Node get(int pos) {
		return args.get(pos);
	}

	@Override
	public void accept(Visitor v) {
		JminorVisitor whileVisitor = (JminorVisitor) v;
		whileVisitor.visit(this);
	}

	@Override
	public ExprList clone(List<Node> args) {
		return new ExprList(args);
	}
}
