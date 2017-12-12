package heap.ast;

public class ASTIntFieldVal extends ASTFieldVal {
	public final int val;

	public ASTIntFieldVal(String fieldName, String src, int val) {
		super(fieldName, src);
		this.val = val;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}