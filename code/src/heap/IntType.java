package heap;

import grammar.Visitor;

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
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}
}