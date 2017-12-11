package grammar;

import java.util.Collections;
import java.util.List;

/**
 * A token is a terminal symbol in the grammar and a leaf node in a derivation
 * tree.
 * 
 * @author romanm
 */
public abstract class Token extends Node {
	protected Token() {
		super(0);
	}

	@Override
	public Token clone(List<Node> args) {
		assert args.size() == 0 : "Attempt to clone a terminal with a non-empty argument list!";
		return this;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}

	@Override
	public final List<Node> getArgs() {
		return Collections.emptyList();
	}

	@Override
	public Nonterminal leftmostNonterminal() {
		return null;
	}

	@Override
	public Node substituteLeftmostNonterminal(Node op) {
		return this;
	}
}