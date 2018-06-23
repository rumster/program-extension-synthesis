package jminor;

import bgu.cs.util.treeGrammar.*;

/**
 * A visitor for derivation trees of the Jminor grammar.
 * 
 * @author romanm
 */
public class JminorVisitor implements Visitor {
	public void visit(True n) {
	}

	public void visit(AndExpr n) {
		for (var sub : n.getArgs()) {
			sub.accept(this);
		}
	}

	public void visit(OrExpr n) {
		for (var sub : n.getArgs()) {
			sub.accept(this);
		}
	}

	public void visit(AssignStmt n) {
		for (var sub : n.getArgs()) {
			sub.accept(this);
		}
	}

	public void visit(ParallelAssign n) {
		for (var sub : n.getArgs()) {
			sub.accept(this);
		}
	}

	public void visit(DerefExpr n) {
		for (var sub : n.getArgs()) {
			sub.accept(this);
		}
	}

	public void visit(EqExpr n) {
		for (var sub : n.getArgs()) {
			sub.accept(this);
		}
	}

	public void visit(LtExpr n) {
		for (var sub : n.getArgs()) {
			sub.accept(this);
		}
	}

	public void visit(IfStmt n) {
		for (var sub : n.getArgs()) {
			sub.accept(this);
		}
	}

	public void visit(NewExpr n) {
		for (var sub : n.getArgs()) {
			sub.accept(this);
		}
	}

	public void visit(NotExpr n) {
		for (var sub : n.getArgs()) {
			sub.accept(this);
		}
	}

	public void visit(SeqStmt n) {
		for (var sub : n.getArgs()) {
			sub.accept(this);
		}
	}

	public void visit(WhileStmt n) {
		for (var sub : n.getArgs()) {
			sub.accept(this);
		}
	}

	public void visit(SkipStmt n) {
	}

	public void visit(RetStmt n) {
	}

	public void visit(NullExpr n) {
	}

	public void visit(IntVal n) {
	}

	public void visit(BooleanVal n) {
	}
	
	public void visit(IntBinOpExpr n) {
		for (var sub : n.getArgs()) {
			sub.accept(this);
		}
	}

	public void visit(RefField n) {
	}

	public void visit(PrimitiveField n) {
	}

	public void visit(VarExpr n) {
	}

	public void visit(RefVar n) {
	}

	public void visit(PrimitiveVar n) {
	}

	public void visit(RefType n) {
	}

	public void visit(ValExpr n) {
		for (var sub : n.getArgs()) {
			sub.accept(this);
		}
	}

	public void visit(Nonterminal n) {
		assert false : "unhandled nonterminal " + n.toString();
	}

	public void visit(Token n) {
		assert false : "unhandled token node " + n.toString();
	}

	public void visit(Node n) {
		assert false : "unhandled node " + n.toString();
	}
}