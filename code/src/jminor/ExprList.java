package jminor;

import java.util.List;

import grammar.Node;
import grammar.Visitor;

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
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}

	@Override
	public ExprList clone(List<Node> args) {
		return new ExprList(args);
	}
}
