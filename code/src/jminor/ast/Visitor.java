package jminor.ast;

/**
 * A visitor over AST nodes.
 * 
 * @author romanm
 */
public abstract class Visitor {
	public void visit(ASTExample n) {
		for (ASTStep step : n.steps) {
			step.accept(this);
		}
	}

	public void visit(ASTDeclField n) {
	}

	public void visit(ASTFun n) {
		for (ASTVarDecl var : n.inputArgs) {
			var.accept(this);
		}
		for (ASTVarDecl var : n.outputArgs) {
			var.accept(this);
		}
		for (ASTVarDecl var : n.temps) {
			var.accept(this);
		}
		for (ASTExample example : n.examples) {
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
		for (ASTDeclField field : n.fields) {
			field.accept(this);
		}
	}

	public void visit(ASTStore n) {
		for (ASTVal val : n.vals) {
			val.accept(this);
		}
	}

	public void visit(ASTVarDecl n) {
	}

	public void visit(ASTRefVarVal n) {
	}

	public void visit(ASTIntVarVal n) {
	}

	////////////////////////////////
	// Statements and expressions //
	////////////////////////////////

	public void visit(ASTVarExpr n) {
	}

	public void visit(ASTNullExpr n) {
	}

	public void visit(ASTIntBinOpExpr n) {
	}

	public void visit(ASTIntValExpr n) {
	}

	public void visit(ASTDerefExpr n) {
	}

	public void visit(ASTAssign n) {
	}

	public void visit(ASTSeq n) {
	}

	public void visit(ASTIf n) {
	}

	public void visit(ASTWhile n) {
	}

	public void visit(ASTAndExpr n) {
	}

	public void visit(ASTOrExpr n) {
	}

	public void visit(ASTNotExpr n) {
	}

	public void visit(ASTEqExpr n) {
	}

	public void visit(ASTBooleanValExpr astBooleanValExpr) {
	}

	public void visit(ASTBooleanVarVal astBooleanVarVal) {
	}

	public void visit(ASTBooleanFieldVal astBooleanFieldVal) {
	}
}