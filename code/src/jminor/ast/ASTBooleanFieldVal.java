package jminor.ast;

public class ASTBooleanFieldVal extends ASTFieldVal {
	public final boolean val;

	public ASTBooleanFieldVal(String fieldName, String src, boolean val) {
		super(fieldName, src);
		this.val = val;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}