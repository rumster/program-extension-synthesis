package heap.ast;

public class ASTField extends AST {
	public final String name;
	public final String typeName;	
	public final boolean ghost;
	
	public ASTField(String name, String type, boolean ghost) {
		this.name = name;
		this.typeName = type;
		this.ghost = ghost;
	}
		
	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}