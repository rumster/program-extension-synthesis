package heap.ast;

/**
 * A visitor over AST nodes.
 * 
 * @author romanm
 */
public abstract class Visitor {
	public void visit(ASTExample n) {
	}

	public void visit(ASTField n) {
	}

	public void visit(ASTFun n) {
	}

	public void visit(ASTRefFieldVal n) {
	}

	public void visit(ASTIntFieldVal n) {
	}

	public void visit(ASTProblem n) {
	}

	public void visit(ASTRefType n) {
	}

	public void visit(ASTStore n) {
	}

	public void visit(ASTVar n) {
	}

	public void visit(ASTRefVarVal astRefVarVal) {
	}

	public void visit(ASTIntVarVal astIntVarVal) {
	}
}