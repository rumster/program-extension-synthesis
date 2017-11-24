package heap;

import java.util.ArrayList;
import java.util.List;

import treeGrammar.*;

/**
 * The operator corresponding to a sequencing statement.
 * 
 * @author romanm
 */
public class OpSeq extends InternalNode {
	protected List<Node> args = new ArrayList<>(2);

	public OpSeq(Node first, Node second) {
		this(first.numOfNonterminals + second.numOfNonterminals);
		args.add(first);
		args.add(second);
	}

	protected OpSeq(int numOfNonterminals) {
		super(numOfNonterminals);
	}

	@Override
	public List<Node> getArgs() {
		return args;
	}

	public ArrayList<Node> getComponents() {
		ArrayList<Node> result = new ArrayList<>();
		for (Node arg : getArgs()) {
			if (arg instanceof OpSeq) {
				OpSeq argSeq = (OpSeq) arg;
				result.addAll(argSeq.getComponents());
			} else {
				result.add(arg);
			}
		}
		return result;
	}

	public OpSeq(List<? extends Node> args) {
		super(countNonterminals(args));
		this.args.addAll(args);
	}

	@Override
	public OpSeq clone(List<Node> args) {
		return new OpSeq(args);
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}
}