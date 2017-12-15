package heap;

import java.util.ArrayList;
import java.util.List;

import grammar.*;

/**
 * A sequencing statement.
 * 
 * @author romanm
 */
public class SequenceStmt extends Node {
	protected List<Node> args = new ArrayList<>(2);

	public SequenceStmt(Node first, Node second) {
		this(first.numOfNonterminals + second.numOfNonterminals);
		args.add(first);
		args.add(second);
	}

	protected SequenceStmt(int numOfNonterminals) {
		super(numOfNonterminals);
	}

	@Override
	public List<Node> getArgs() {
		return args;
	}

	public ArrayList<Node> getComponents() {
		ArrayList<Node> result = new ArrayList<>();
		for (Node arg : getArgs()) {
			if (arg instanceof SequenceStmt) {
				SequenceStmt argSeq = (SequenceStmt) arg;
				result.addAll(argSeq.getComponents());
			} else {
				result.add(arg);
			}
		}
		return result;
	}

	public SequenceStmt(List<Node> args) {
		super(countNonterminals(args));
		this.args.addAll(args);
	}

	@Override
	public SequenceStmt clone(List<Node> args) {
		return new SequenceStmt(args);
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}
}