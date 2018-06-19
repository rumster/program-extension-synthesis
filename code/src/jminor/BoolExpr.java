package jminor;

import java.util.List;

import bgu.cs.util.treeGrammar.Node;
import pexyn.Domain.Guard;

/**
 * The base class of Boolean expressions.
 * 
 * @author romanm
 */
public abstract class BoolExpr extends Expr implements Guard {
	protected BoolExpr(List<Node> nodes) {
		super(nodes);
	}

	protected BoolExpr(Node... nodes) {
		super(nodes);
	}
}