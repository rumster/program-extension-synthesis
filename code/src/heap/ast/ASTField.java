package heap.ast;

public class ASTField extends AST {
	public final String name;
	public final String typeName;	
	
	public ASTField(String name, String type) {
		this.name = name;
		this.typeName = type;
	}
		
	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}