package heap.ast;

import java.util.List;

/**
 * An example is a sequence of steps, the first of which must be a state.
 * 
 * @author romanm
 */
public class ASTExample extends AST {
	public final List<ASTStep> steps;
	public boolean isTest = false;

	public ASTExample(List<ASTStep> steps) {
		this.steps = steps;
	}

	public ASTStore input() {
		return (ASTStore) steps.get(0);
	}

	public ASTStore goal() {
		return (ASTStore) steps.get(steps.size() - 1);
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}