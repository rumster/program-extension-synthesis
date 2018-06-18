package jminor.ast;

public class ASTRefFieldVal extends ASTFieldVal {
	public final String val;

	public ASTRefFieldVal(String fieldName, String src, String val) {
		super(fieldName, src);
		this.val = val;
	}
	
	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}