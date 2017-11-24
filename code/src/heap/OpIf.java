package heap;

import java.util.ArrayList;
import java.util.List;

import treeGrammar.*;

/**
 * The operator corresponding to an 'if-then-else' statement.
 * 
 * @author romanm
 */
public class OpIf extends InternalNode {
	protected List<Node> args = new ArrayList<>(3);

	public OpIf(Node condNode, Node thenNode, Node elseNode) {
		super(condNode.numOfNonterminals + thenNode.numOfNonterminals + (elseNode !=null ? elseNode.numOfNonterminals : 0));
		args.add(condNode);
		args.add(thenNode);
		args.add(elseNode);
	}

	protected OpIf(int numOfNonterminals) {
		super(numOfNonterminals);
	}

	@Override
	public List<Node> getArgs() {
		return args;
	}

	public Node getCond() {
		return args.get(0);
	}

	public Node getThen() {
		return args.get(1);
	}

	public Node getElseNode() {
		return args.get(2);
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}

	protected OpIf(List<Node> args) {
		super(countNonterminals(args));
		assert args.size() == 3 : "Illegal number of arguments for " + getClass().getSimpleName() + ": " + args.size()
				+ "!";
		this.args = args;
	}

	@Override
	public OpIf clone(List<Node> args) {
		return new OpIf(args);
	}
}