package heap;

import heap.State.Val;
import treeGrammar.Visitor;

/**
 * A terminal for integer-valued fields.
 * 
 * @author romanm
 */
public class IntField extends Field {
	public IntField(String name, RefType srcType) {
		super(name, srcType, IntType.v);
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}

	@Override
	public Val getDefaultVal() {
		return State.Int.ZERO;
	}
}