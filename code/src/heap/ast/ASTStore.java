package heap.ast;

import java.util.List;

/**
 * A store node.
 * 
 * @author romanm
 */
public class ASTStore extends ASTStep {
	public final List<ASTVal> vals;

	public ASTStore(List<ASTVal> vals) {
		this.vals = vals;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}