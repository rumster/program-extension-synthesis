package jminor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import bgu.cs.util.treeGrammar.*;

/**
 * A sequencing statement.
 * 
 * @author romanm
 */
public class SeqStmt extends Stmt {
	public SeqStmt(Node first, Node second) {
		super(first, second);
		assertNumOfArgs(2);
	}

	public SeqStmt(Collection<Node> args) {
		super(args);
		assertNumOfArgs(2);
	}

	public Node getFirst() {
		return args.get(0);
	}

	public Node getSecond() {
		return args.get(1);
	}

	public ArrayList<Node> getComponents() {
		ArrayList<Node> result = new ArrayList<>();
		for (Node arg : getArgs()) {
			if (arg instanceof SeqStmt) {
				SeqStmt argSeq = (SeqStmt) arg;
				result.addAll(argSeq.getComponents());
			} else {
				result.add(arg);
			}
		}
		return result;
	}

	@Override
	public SeqStmt clone(List<Node> args) {
		return new SeqStmt(args);
	}

	@Override
	public void accept(Visitor v) {
		JminorVisitor whileVisitor = (JminorVisitor) v;
		whileVisitor.visit(this);
	}
}