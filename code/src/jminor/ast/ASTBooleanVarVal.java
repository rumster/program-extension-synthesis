package jminor.ast;

public class ASTBooleanVarVal extends ASTVarVal {
	public final boolean val;

	public ASTBooleanVarVal(String varName, boolean val) {
		super(varName);
		this.val = val;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}
