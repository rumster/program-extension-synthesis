package heap.ast;

/**
 * A variable declaration node.
 * 
 * @author romanm
 */
public class ASTVarDecl extends AST {
	public final String name;
	public final String type;
	public boolean readonly;

	public ASTVarDecl(String name, String type) {
		this.name = name;
		this.type = type;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}