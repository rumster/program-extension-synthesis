package heap;

import java.util.Collection;

import grammar.Node;

/**
 * A base class for basic (i.e., 3-address code) PWhile statements.
 * 
 * @author romanm
 */
public abstract class BasicStmt extends Node {
	protected BasicStmt(int numOfNonterminals) {
		super(numOfNonterminals);
	}

	/**
	 * Tests whether this statement can be applied to the given state.
	 */
	public abstract boolean enabled(Store s);

	/**
	 * Applies this statement to the given state.
	 */
	public abstract Collection<Store> apply(Store s);
}