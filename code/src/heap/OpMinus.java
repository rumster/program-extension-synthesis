package heap;

import java.util.ArrayList;
import java.util.List;

import treeGrammar.*;

/**
 * The operator corresponding to arithmetic negation (unary operator).
 *
 */
public class OpMinus extends InternalNode {
	protected List<Node> args = new ArrayList<>(1);

	public OpMinus(int numOfNonterminals) {
		super(numOfNonterminals);
	}

	public OpMinus(Node sub) {
		super(sub.numOfNonterminals);
		args.add(sub);
	}

	@Override
	public List<Node> getArgs() {
		return args;
	}

	public Node getSub() {
		return args.get(0);
	}

	protected OpMinus(List<Node> args) {
		super(countNonterminals(args));
		assert args.size() == 1 : "Illegal number of arguments for " + getClass().getSimpleName() + ": " + args.size()
				+ "!";
		this.args = args;
	}

	@Override
	public InternalNode clone(List<Node> args) {
		return new OpMinus(args);
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}
}