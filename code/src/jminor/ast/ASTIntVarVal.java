package jminor.ast;

public class ASTIntVarVal extends ASTVarVal {
	public final int val;

	public ASTIntVarVal(String varName, int val) {
		super(varName);
		this.val = val;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}
