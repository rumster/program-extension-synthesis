package treeGrammar;

import java.util.Collections;
import java.util.List;

/**
 * A nonterminal wrapped inside an operator.
 * 
 * @author romanm
 */
public class OpNonterminal extends Operator {

	public final Nonterminal n;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((n == null) ? 0 : n.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof OpNonterminal))
			return false;
		OpNonterminal other = (OpNonterminal) obj;
		if (n == null) {
			if (other.n != null)
				return false;
		} else if (!n.equals(other.n))
			return false;
		return true;
	}

	public OpNonterminal(Nonterminal n) {
		super(1);
		this.n = n;
	}

	@Override
	public List<Node> getArgs() {
		return Collections.singletonList(n);
	}

	@Override
	public Operator substituteLeftmost(Operator op) {
		return op;
	}

	@Override
	public Operator clone(List<Node> args) {
		assert args.size() == 1;
		return new OpNonterminal(args);
	}

	protected OpNonterminal(List<Node> args) {
		super(countNonterminals(args));
		assert args.size() == 1 : "Illegal number of arguments for " + getClass().getSimpleName() + ": " + args.size()
				+ "!";
		this.n = (Nonterminal) args.get(0);
	}

	@Override
	public void accept(Visitor v) {
		v.visit(n);
	}
}