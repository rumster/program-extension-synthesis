package jminor;

import java.util.List;

import grammar.Node;
import grammar.Visitor;

/**
 * A variable expression.
 * 
 * @author romanm
 */
public class VarExpr extends Expr {
	public VarExpr(Var v) {
		super(v);
	}

	public Var getVar() {
		return (Var) args.get(0);
	}
	
	public String varName() {
		return getVar().name;
	}
	
	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}

	protected VarExpr(List<Node> args) {
		super(args);
		assertNumOfArgs(1);
	}

	@Override
	public VarExpr clone(List<Node> args) {
		assert args.size() == 1;
		return new VarExpr(args);
	}
}