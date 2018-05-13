package heap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import gp.Domain.Update;
import grammar.Node;

/**
 * A base class for PWhile statements.
 * 
 * @author romanm
 */
public abstract class Stmt extends Node implements Update {
	protected final List<Node> args;

	@Override
	public final List<Node> getArgs() {
		return args;
	}

	protected Stmt(Collection<Node> nodes) {
		super(countNonterminals(nodes));
		this.args = Collections.unmodifiableList(new ArrayList<>(nodes));
	}

	protected Stmt(Node... args) {
		super(countNonterminals(args));
		var argList = new ArrayList<Node>(args.length);
		for (Node n : args) {
			argList.add(n);
		}
		this.args = Collections.unmodifiableList(argList);
	}

	protected Stmt() {
		super(0);
		this.args = Collections.emptyList();
	}

	protected Stmt(int numOfNonterminals) {
		super(numOfNonterminals);
		this.args = Collections.emptyList();
	}

	/**
	 * Tests whether this statement can be applied to the given state.
	 */
	public boolean enabled(Store store) {
		Store result = PWhileInterpreter.v.run(this, store, PWhileInterpreter.v.guessMaxSteps(this, store)).get();
		return !(result instanceof Store.ErrorStore);
	}
}