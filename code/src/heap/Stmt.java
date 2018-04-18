package heap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import grammar.Node;

/**
 * A base class for PWhile statements.
 * 
 * @author romanm
 */
public abstract class Stmt extends Node {
	protected final List<Node> args = new ArrayList<>(2);

	@Override
	public final List<Node> getArgs() {
		return args;
	}

	protected Stmt(Collection<Node> nodes) {
		super(countNonterminals(nodes));
		this.args.addAll(args);
	}

	protected Stmt(Node... args) {
		super(countNonterminals(args));
		for (Node arg : args) {
			this.args.add(arg);
		}
	}

	protected Stmt() {
		super(0);
	}

	protected Stmt(int numOfNonterminals) {
		super(numOfNonterminals);
	}

	/**
	 * Tests whether this statement can be applied to the given state.
	 */
	public boolean enabled(Store store) {
		return !(apply(store) instanceof Store.ErrorStore);
	}

	public Store apply(Store store) {
		Store result = PWhileInterpreter.v.apply(this, store);
		return result;
	}
}