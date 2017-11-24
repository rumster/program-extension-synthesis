package heap;

import treeGrammar.*;

/**
 * A visitor for derivation trees of the PWhile grammar.
 * 
 * @author romanm
 */
public class PWhileVisitor implements Visitor {
	public void visit(OpAnd n) {
	}

	public void visit(OpOr n) {
	}

	public void visit(OpAssgn n) {
	}

	public void visit(OpDeref n) {
	}

	public void visit(OpEq n) {
	}

	public void visit(OpLeq n) {
	}

	public void visit(OpLt n) {
	}

	public void visit(OpIf n) {
	}

	public void visit(OpNew n) {
	}

	public void visit(OpNot n) {
	}

	public void visit(OpSeq n) {
	}

	public void visit(OpWhile n) {
	}

	public void visit(OpSkip n) {
	}

	public void visit(Null n) {
	}

	public void visit(Int n) {
	}

	public void visit(OpMinus n) {
	}

	public void visit(OpPlus n) {
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

	public void visit(Terminal n) {
		assert false : "unhandled terminal " + n.toString();
	}

	public void visit(InternalNode n) {
		assert false : "unhandled operator " + n.toString();
	}

	public void visit(OpNonterminal n) {
		assert false : "unhandled nonterminal " + n.toString();
	}
}