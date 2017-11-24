package heap;

import java.util.ArrayList;
import java.util.List;

import treeGrammar.*;

/**
 * The operator corresponding to an object field access.
 * 
 * @author romanm
 */
public class OpDeref extends InternalNode {
	protected List<Node> args = new ArrayList<>(2);

	public OpDeref(Node lhs, Field pfield) {
		super(lhs.numOfNonterminals);
		args.add(lhs);
		args.add(pfield);
	}

	protected OpDeref(int numOfNonterminals) {
		super(numOfNonterminals);
	}

	public int depth() {
		Node lhs = getLhs();
		if (lhs instanceof OpDeref) {
			return 1 + ((OpDeref) lhs).depth();
		} else {
			return 1;
		}
	}
	
	public Type dstType(){
		Field f = getField();
		return f.getDstType();
	}

	@Override
	public List<Node> getArgs() {
		return args;
	}

	public Node getLhs() {
		return args.get(0);
	}

	public Field getField() {
		return (Field) args.get(1);
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}

	protected OpDeref(List<Node> args) {
		super(countNonterminals(args));
		assert args.size() == 2 : "Illegal number of arguments for " + getClass().getSimpleName() + ": " + args.size()
				+ "!";
		this.args = args;
	}

	@Override
	public OpDeref clone(List<Node> args) {
		return new OpDeref(args);
	}
}