package jminor;

import bgu.cs.util.treeGrammar.Visitor;

/**
 * Represents an object type.
 * 
 * @author romanm
 */
public class IntType extends Type {
	public static final IntType v = new IntType();

	protected IntType() {
		super("int");
	}

	@Override
	public void accept(Visitor v) {
		JminorVisitor whileVisitor = (JminorVisitor) v;
		whileVisitor.visit(this);
	}
	
	@Override
	public boolean equals(Object o) {
		return this == o;
	}
}