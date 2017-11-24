package heap;

import treeGrammar.Visitor;

/**
 * A terminal representing an integer type variable.
 * 
 * @author romanm
 */
public class IntVar extends Var {
	public IntVar(String name, VarRole role, boolean out, boolean readonly) {
		super(name, role, out, readonly);
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}
}