package jminor;

import bgu.cs.util.treeGrammar.Visitor;

/**
 * A terminal for reference fields.
 * 
 * @author romanm
 */
public class RefField extends Field {
	public RefField(String name, RefType srcType, RefType dstType, boolean ghost) {
		super(name, srcType, dstType, ghost);
	}

	public RefType getDstType() {
		return (RefType) dstType;
	}

	@Override
	public void accept(Visitor v) {
		JminorVisitor whileVisitor = (JminorVisitor) v;
		whileVisitor.visit(this);
	}

	@Override
	public Val getDefaultVal() {
		return Obj.NULL;
	}

	@Override
	public String toString() {
		return name;
	}
}