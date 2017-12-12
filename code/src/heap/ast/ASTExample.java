package heap.ast;

public class ASTExample extends AST {
	public final ASTStore input;
	public final ASTStore goal;
	
	public ASTExample(ASTStore input, ASTStore goal) {
		this.input = input;
		this.goal = goal;
	}
	
	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}