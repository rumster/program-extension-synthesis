package jminor;

import bgu.cs.util.treeGrammar.CostFun;
import bgu.cs.util.treeGrammar.Node;
import bgu.cs.util.treeGrammar.Nonterminal;

/**
 * A cost function that fines selected "bad" conditions by assigning them a very
 * high cost.
 * 
 * @author romanm
 */
public class CostBadConditions implements CostFun {
	public static final CostBadConditions v = new CostBadConditions();

	protected boolean good;

	@Override
	public float apply(Node root) {
		good = true;
		check(root);
		if (!good)
			return CostFun.INFINITY_COST;
		else
			return 0;
	}

	protected void check(Node n) {
		if (doubleNegation(n) || trivialEq(n) || trivialLt(n) || deepDeref(n)) {
			good = false;
			return;
		}

		for (Node arg : n.getArgs()) {
			check(arg);
			if (!good)
				return;
		}
	}

	protected boolean deepDeref(Node op) {
		if (op instanceof DerefExpr && ((DerefExpr) op).depth() > 3)
			return true;
		return false;
	}

	protected boolean trivialEq(Node op) {
		if (op instanceof EqExpr) {
			EqExpr oeq = (EqExpr) op;
			if (!(oeq.getLhs() instanceof Nonterminal) && !(oeq.getRhs() instanceof Nonterminal)) {
				if (Renderer.render(oeq.getLhs()).equals(Renderer.render(oeq.getRhs())))
					return true;
			}
		}
		return false;
	}

	protected boolean trivialLt(Node op) {
		if (op instanceof LtExpr) {
			LtExpr oeq = (LtExpr) op;
			if (!(oeq.getLhs() instanceof Nonterminal) && !(oeq.getRhs() instanceof Nonterminal)) {
				if (Renderer.render(oeq.getLhs()).equals(Renderer.render(oeq.getRhs())))
					return true;
			}
		}
		return false;
	}

	protected boolean doubleNegation(Node op) {
		if (op instanceof NotExpr) {
			NotExpr on = (NotExpr) op;
			if (on.getSub() instanceof NotExpr) {
				return true;
			}
		}
		return false;
	}
}
