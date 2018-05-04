package heap;

import java.util.List;

import grammar.Node;
import grammar.Visitor;

public class True extends BoolExpr {
	public static True v = new True();
	
	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}

	@Override
	public Node clone(List<Node> args) {
		return this;
	}
	
	private True() {			
	}
}