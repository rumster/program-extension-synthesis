package jminor;

import grammar.Visitor;

/**
 * An integer-valued variable.
 * 
 * @author romanm
 */
public class IntVar extends Var {
	public IntVar(String name, VarRole role, boolean out, boolean readonly) {
		super(name, IntType.v, role, out, readonly);
	}

	@Override
	public void accept(Visitor v) {
		JminorVisitor whileVisitor = (JminorVisitor) v;
		whileVisitor.visit(this);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}