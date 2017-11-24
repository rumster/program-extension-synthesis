package heap;

import java.util.ArrayList;
import java.util.List;

import treeGrammar.Node;
import treeGrammar.InternalNode;
import treeGrammar.Visitor;

/**
 * The operator corresponding to a variable-to-variable assignment statement.
 * 
 * @author romanm
 */
public class OpAssgn extends InternalNode {
	protected List<Node> args = new ArrayList<>(2);

	public OpAssgn(Node lhs, Node rhs) {
		super(lhs.numOfNonterminals + rhs.numOfNonterminals);
		args.add(lhs);
		args.add(rhs);
	}

	protected OpAssgn(int numOfNonterminals) {
		super(numOfNonterminals);
	}

	@Override
	public List<Node> getArgs() {
		return args;
	}

	public Node getLhs() {
		return args.get(0);
	}

	public Node getRhs() {
		return args.get(1);
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}

	protected OpAssgn(List<Node> args) {
		super(countNonterminals(args));
		assert args.size() == 2 : "Illegal number of arguments for " + getClass().getSimpleName() + ": " + args.size()
				+ "!";
		this.args = args;
	}

	@Override
	public OpAssgn clone(List<Node> args) {
		return new OpAssgn(args);
	}
}