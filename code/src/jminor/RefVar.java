package jminor;

import grammar.Visitor;

/**
 * A terminal representing a reference variable.
 * 
 * @author romanm
 */
public class RefVar extends Var {
	public RefVar(String name, RefType type, VarRole role, boolean out, boolean readonly) {
		super(name, type, role, out, readonly);
	}

	public RefVar(String name, RefType type) {
		this(name, type, VarRole.TEMP, false, false);
	}

	@Override
	public void accept(Visitor v) {
		JminorVisitor whileVisitor = (JminorVisitor) v;
		whileVisitor.visit(this);
	}

	@Override
	public RefType getType() {
		return (RefType) type;
	}
}