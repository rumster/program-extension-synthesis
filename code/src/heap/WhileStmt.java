package heap;

import java.util.ArrayList;
import java.util.List;

import grammar.Node;
import grammar.Visitor;

/**
 * A looping statement.
 * 
 * @author romanm
 */
public class WhileStmt extends Node {
	protected List<Node> args = new ArrayList<>(2);

	public WhileStmt(Node condNode, Node bodyNode) {
		super(condNode.numOfNonterminals + bodyNode.numOfNonterminals);
		args.add(condNode);
		args.add(bodyNode);
	}

	protected WhileStmt(int numOfNonterminals) {
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

	protected WhileStmt(List<Node> args) {
		super(countNonterminals(args));
		assert args.size() == 2 : "Illegal number of arguments for " + getClass().getSimpleName() + ": " + args.size()
				+ "!";
		this.args = args;
	}

	@Override
	public WhileStmt clone(List<Node> args) {
		return new WhileStmt(args);
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}
}