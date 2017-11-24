package heap;

import java.util.ArrayList;
import java.util.List;

import treeGrammar.*;

/**
 * The operator corresponding to an object allocation.
 * 
 * @author romanm
 */
public class OpNew extends InternalNode {
	protected List<Node> args = new ArrayList<>(2);

	public OpNew(RefType type) {
		super(1);
		args.add(type);
	}

	protected OpNew(int numOfNonterminals) {
		super(numOfNonterminals);
	}

	@Override
	public List<Node> getArgs() {
		return args;
	}

	public RefType getType() {
		return (RefType) args.get(0);
	}

	protected OpNew(List<Node> args) {
		super(countNonterminals(args));
		assert args.size() == 1 : "Illegal number of arguments for " + getClass().getSimpleName() + ": " + args.size()
				+ "!";
		this.args = args;
	}

	@Override
	public OpNew clone(List<Node> args) {
		return new OpNew(args);
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}
}