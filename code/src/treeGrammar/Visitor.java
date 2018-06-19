package grammar;

/**
 * A visitor over derivation trees.
 * 
 * @author romanm
 */
public interface Visitor {
	public void visit(Nonterminal n);

	public void visit(Token n);

	public void visit(Node n);
}