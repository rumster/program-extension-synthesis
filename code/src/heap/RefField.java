package heap;

import heap.State.Obj;
import heap.State.Val;
import treeGrammar.Visitor;

/**
 * A terminal for reference fields.
 * 
 * @author romanm
 */
public class RefField extends Field {
	public RefField(String name, RefType srcType, RefType dstType) {
		super(name, srcType, dstType);
	}

	@Override
	public RefType getDstType() {
		return (RefType) dstType;
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}

	@Override
	public Val getDefaultVal() {
		return Obj.NULL;
	}
}