package heap.ast;

public class ASTField extends AST {
	public final String name;
	public final String type;
	
	public ASTField(String name, String type) {
		this.name = name;
		this.type = type;
	}
		
	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}