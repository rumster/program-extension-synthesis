package jminor;

import java.util.List;

import bgu.cs.util.treeGrammar.Node;
import bgu.cs.util.treeGrammar.Visitor;

/**
 * Represents the always-true Boolean expression.
 * 
 * @author romanm
 */
public class True extends BoolExpr {
	public static True v = new True();

	@Override
	public void accept(Visitor v) {
		JminorVisitor whileVisitor = (JminorVisitor) v;
		whileVisitor.visit(this);
	}

	@Override
	public Node clone(List<Node> args) {
		return this;
	}

	@Override
	public String toString() {
		return "true";
	}

	@Override
	public boolean equals(Object o) {
		return o == this;
	}

	private True() {
	}
}