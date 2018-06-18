package jminor;

import grammar.Visitor;

/**
 * Represents the Boolean type.
 * 
 * @author romanm
 */
public class BoolType extends Type {
	public static final BoolType v = new BoolType();

	protected BoolType() {
		super("boolean");
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}

	@Override
	public boolean equals(Object o) {
		return o == this;
	}
}