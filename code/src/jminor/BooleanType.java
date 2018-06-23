package jminor;

import bgu.cs.util.treeGrammar.Visitor;

/**
 * Represents the Boolean type.
 * 
 * @author romanm
 */
public class BooleanType extends Type implements PrimitiveType {
	public static final BooleanType v = new BooleanType();

	protected BooleanType() {
		super("boolean");
	}

	@Override
	public void accept(Visitor v) {
		JminorVisitor whileVisitor = (JminorVisitor) v;
		whileVisitor.visit(this);
	}

	@Override
	public boolean equals(Object o) {
		return o == this;
	}
}