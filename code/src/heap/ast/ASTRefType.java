package heap.ast;

import java.util.List;

public class ASTRefType extends AST {
	public final String name;
	public final List<ASTField> fields;

	public ASTRefType(String name, List<ASTField> fields) {
		assert name != null && fields != null;
		this.name = name;
		this.fields = fields;
	}
	
	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}