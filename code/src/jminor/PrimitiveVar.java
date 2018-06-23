package jminor;

import bgu.cs.util.treeGrammar.Visitor;

/**
 * An primitive-typed variable.
 * 
 * @author romanm
 */
public class PrimitiveVar extends Var {
	public PrimitiveVar(String name, Type type, VarRole role, boolean out, boolean readonly) {
		super(name, type, role, out, readonly);
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