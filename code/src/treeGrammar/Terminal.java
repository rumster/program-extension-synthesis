package treeGrammar;

import java.util.Collections;
import java.util.List;

/**
 * A terminal.
 * 
 * @author romanm
 */
public abstract class Terminal extends InternalNode {
	protected Terminal() {
		super(0);
	}

	public Terminal clone(List<Node> args) {
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
}