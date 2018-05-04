package heap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import grammar.Node;

/**
 * The base class of PWhile expressions.
 * 
 * @author romanm
 */
public abstract class Expr extends Node {
	protected final List<Node> args = new ArrayList<>(2);

	@Override
	public final List<Node> getArgs() {
		return args;
	}

	protected Expr(Collection<Node> nodes) {
		super(countNonterminals(nodes));
		args.addAll(nodes);
	}

	protected Expr(Node... nodes) {
		super(countNonterminals(nodes));
		for (Node n : nodes) {
			args.add(n);
		}
	}
}