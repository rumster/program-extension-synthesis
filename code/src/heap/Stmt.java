package heap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import gp.Domain.Update;
import grammar.Node;

/**
 * A base class for PWhile statements.
 * 
 * @author romanm
 */
public abstract class Stmt extends Node implements Update {
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
		Store result = PWhileInterpreter.v.apply(this, store);
		return !(result instanceof Store.ErrorStore);
	}
}