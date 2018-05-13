package heap;

import grammar.*;

/**
 * A visitor for derivation trees of the PWhile grammar.
 * 
 * @author romanm
 */
public class PWhileVisitor implements Visitor {
	public void visit(True n) {
	}

	public void visit(AndExpr n) {
	}

	public void visit(OrExpr n) {
	}

	public void visit(AssignStmt n) {
	}

	public void visit(ParallelAssign n) {
	}

	public void visit(DerefExpr n) {
	}

	public void visit(EqExpr n) {
	}

	public void visit(LtExpr n) {
	}

	public void visit(IfStmt n) {
	}

	public void visit(NewExpr n) {
	}

	public void visit(NotExpr n) {
	}

	public void visit(SeqStmt n) {
	}

	public void visit(WhileStmt n) {
	}

	public void visit(SkipStmt n) {
	}

	public void visit(RetStmt n) {
	}

	public void visit(NullExpr n) {
	}

	public void visit(IntVal n) {
	}

	public void visit(IntBinOpExpr n) {
	}

	public void visit(RefField n) {
	}

	public void visit(IntField n) {
	}

	public void visit(VarExpr n) {
	}

	public void visit(RefVar n) {
	}

	public void visit(IntVar n) {
	}

	public void visit(RefType n) {
	}

	public void visit(ValExpr n) {
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