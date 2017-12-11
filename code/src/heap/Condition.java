package heap;

import grammar.Node;

/**
 * The base class of Boolean expressions.
 * 
 * @author romanm
 */
public abstract class Condition extends Node {
	protected Condition(int numOfNonterminals) {
		super(numOfNonterminals);
	}
}