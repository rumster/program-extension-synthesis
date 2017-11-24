package heap;

import treeGrammar.Visitor;

/**
 * A terminal representing a reference variable.
 * 
 * @author romanm
 */
public class RefVar extends Var {
	public final RefType type;

	public RefVar(String name, RefType type, VarRole role, boolean out, boolean readonly) {
		super(name, role, out, readonly);
		this.type = type;
	}

	public RefVar(String name, RefType type) {
		this(name, type, VarRole.TEMPORARY, false, false);
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}
}