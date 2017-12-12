package heap.ast;

import java.util.List;

public class ASTStore extends AST {
	public final List<ASTFieldVal> fieldVals;

	public ASTStore(List<ASTFieldVal> fieldVals) {
		this.fieldVals = fieldVals;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}