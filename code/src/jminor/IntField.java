package jminor;

import grammar.Visitor;

/**
 * A terminal for integer-valued fields.
 * 
 * @author romanm
 */
public class IntField extends Field {
	public IntField(String name, RefType srcType, boolean ghost) {
		super(name, srcType, IntType.v, ghost);
	}

	@Override
	public void accept(Visitor v) {
		JminorVisitor whileVisitor = (JminorVisitor) v;
		whileVisitor.visit(this);
	}

	@Override
	public Val getDefaultVal() {
		return IntVal.ZERO;
	}
	
	@Override
	public String toString() {
		return name;
	}
}