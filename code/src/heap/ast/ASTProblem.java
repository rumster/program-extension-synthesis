package heap.ast;

import java.util.List;

public class ASTProblem extends AST {
	public List<AST> elements;
	
	public ASTProblem(List<AST> elements) {
		this.elements = elements;
	}
	
	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}