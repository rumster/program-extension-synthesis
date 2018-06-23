package jminor;

import bgu.cs.util.treeGrammar.Visitor;

/**
 * A primitive-typed field.
 * 
 * @author romanm
 */
public class PrimitiveField extends Field {
	public PrimitiveField(String name, RefType srcType, Type dstType, boolean ghost) {
		super(name, srcType, dstType, ghost);
	}

	@Override
	public void accept(Visitor v) {
		JminorVisitor whileVisitor = (JminorVisitor) v;
		whileVisitor.visit(this);
	}

	@Override
	public Val getDefaultVal() {
		if (dstType == IntType.v) {
			return IntVal.ZERO;
		} else if (dstType == BooleanType.v) {
			return BooleanVal.FALSE;
		} else {
			throw new Error("Unsupported type: " + dstType.getName());
		}
	}

	@Override
	public String toString() {
		return name;
	}
}