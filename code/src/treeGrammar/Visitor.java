package treeGrammar;

/**
 * A visitor over derivation trees.
 * 
 * @author romanm
 */
public interface Visitor {
	public void visit(Nonterminal n);

	public void visit(Terminal n);

	public void visit(InternalNode n);
	
	public void visit(OpNonterminal n);
	
}