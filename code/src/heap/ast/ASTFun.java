package heap.ast;

import java.util.List;

public class ASTFun extends AST {
	public final String name;
	public final List<ASTVar> inputArgs;
	public final List<ASTVar> outputArgs;
	public final List<ASTVar> temps;
	public final List<ASTExample> examples;

	public ASTFun(String name, List<ASTVar> inputArgs, List<ASTVar> outputArgs, List<ASTVar> temps, List<ASTExample> examples) {
		this.name = name;
		this.inputArgs = inputArgs;
		this.outputArgs = outputArgs;
		this.temps = temps;
		this.examples = examples;
	}
	
	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}