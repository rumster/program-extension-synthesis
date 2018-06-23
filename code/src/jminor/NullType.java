package jminor;

import bgu.cs.util.treeGrammar.Visitor;

/**
 * Represents the null type.
 * 
 * @author romanm
 */
public class NullType extends RefType {
	public static final NullType v = new NullType();

	private NullType() {
		super("null");
	}

	public void add(Field field) {
		throw new UnsupportedOperationException("Encountered attempt to add a field to the null type!");
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