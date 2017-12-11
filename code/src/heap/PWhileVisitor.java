package heap;

import grammar.*;

/**
 * A visitor for derivation trees of the PWhile grammar.
 * 
 * @author romanm
 */
public class PWhileVisitor implements Visitor {
	public void visit(AndExpr n) {
	}

	public void visit(OrExpr n) {
	}

	public void visit(AssignStmt n) {
	}

	public void visit(DerefExpr n) {
	}

	public void visit(EqExpr n) {
	}

	public void visit(LeqExpr n) {
	}

	public void visit(LtExpr n) {
	}

	public void visit(IfStmt n) {
	}

	public void visit(NewExpr n) {
	}

	public void visit(NotExpr n) {
	}

	public void visit(SequenceStmt n) {
	}

	public void visit(WhileStmt n) {
	}

	public void visit(SkipStmt n) {
	}

	public void visit(NullExpr n) {
	}

	public void visit(IntVal n) {
	}

	public void visit(MinusExpr n) {
	}

	public void visit(PlusExpr n) {
	}

	public void visit(RefField n) {
	}

	public void visit(IntField n) {
	}

	public void visit(RefVar n) {
	}

	public void visit(IntVar n) {
	}

	public void visit(RefType n) {
	}

	public void visit(Nonterminal n) {
		assert false : "unhandled terminal " + n.toString();
	}

	public void visit(Token n) {
		assert false : "unhandled terminal " + n.toString();
	}

	public void visit(Node n) {
		assert false : "unhandled operator " + n.toString();
	}
}