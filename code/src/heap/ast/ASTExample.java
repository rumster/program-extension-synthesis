package heap.ast;

import java.util.List;

public class ASTExample extends AST {
	public final List<ASTStore> steps;

	public ASTExample(List<ASTStore> steps) {
		this.steps = steps;
	}

	public ASTStore input() {
		return steps.get(0);
	}

	public ASTStore goal() {
		return steps.get(steps.size() - 1);
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}