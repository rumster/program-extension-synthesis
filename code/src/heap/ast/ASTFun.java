package heap.ast;

import java.util.List;

public class ASTFun extends AST {
	public final String name;
	public final List<ASTVarDecl> inputArgs;
	public final List<ASTVarDecl> outputArgs;
	public final List<ASTVarDecl> temps;
	public final ASTStmt optProg;
	public final List<ASTExample> examples;

	public ASTFun(String name, List<ASTVarDecl> inputArgs, List<ASTVarDecl> outputArgs, List<ASTVarDecl> temps,
			ASTStmt optProg, List<ASTExample> examples) {
		this.name = name;
		this.inputArgs = inputArgs;
		this.outputArgs = outputArgs;
		this.temps = temps;
		this.optProg = optProg;
		this.examples = examples;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}