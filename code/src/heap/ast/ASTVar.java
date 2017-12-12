package heap.ast;

public class ASTVar extends AST {
	public final String name;
	public final String type;
	public boolean readonly;

	public ASTVar(String name, String type) {
		this.name = name;
		this.type = type;
	}
	
	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}