package heap.ast;

/**
 * A visitor over AST nodes.
 * 
 * @author romanm
 */
public abstract class Visitor {
	public void visit(ASTExample n) {
		n.input.accept(this);
		n.goal.accept(this);
	}

	public void visit(ASTField n) {		
	}

	public void visit(ASTFun n) {
		for (ASTVar var: n.inputArgs) {
			var.accept(this);
		}
		for (ASTVar var: n.outputArgs) {
			var.accept(this);
		}
		for (ASTVar var: n.temps) {
			var.accept(this);
		}
		for (ASTExample example: n.examples) {
			example.accept(this);
		}
	}

	public void visit(ASTRefFieldVal n) {
	}

	public void visit(ASTIntFieldVal n) {
	}

	public void visit(ASTProblem n) {
		for (AST elem : n.elements) {
			elem.accept(this);
		}
	}

	public void visit(ASTRefType n) {
		for (ASTField field: n.fields) {
			field.accept(this);
		}
	}

	public void visit(ASTStore n) {
		for (ASTVal val: n.vals) {
			val.accept(this);
		}
	}

	public void visit(ASTVar n) {
	}

	public void visit(ASTRefVarVal n) {
	}

	public void visit(ASTIntVarVal n) {
	}
}