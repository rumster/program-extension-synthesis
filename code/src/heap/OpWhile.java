package heap;

import java.util.ArrayList;
import java.util.List;

import treeGrammar.*;

/**
 * The operator corresponding to a While statement.
 * 
 * @author romanm
 */
public class OpWhile extends InternalNode {
	protected List<Node> args = new ArrayList<>(2);

	public OpWhile(Node condNode, Node bodyNode) {
		super(condNode.numOfNonterminals + bodyNode.numOfNonterminals);
		args.add(condNode);
		args.add(bodyNode);
	}

	protected OpWhile(int numOfNonterminals) {
		super(numOfNonterminals);
	}

	@Override
	public List<Node> getArgs() {
		return args;
	}

	public Node getCond() {
		return args.get(0);
	}

	public Node getBody() {
		return args.get(1);
	}

	protected OpWhile(List<Node> args) {
		super(countNonterminals(args));
		assert args.size() == 2 : "Illegal number of arguments for " + getClass().getSimpleName() + ": " + args.size()
				+ "!";
		this.args = args;
	}

	@Override
	public OpWhile clone(List<Node> args) {
		return new OpWhile(args);
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}
}