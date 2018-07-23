package pexyn.grammarInference;

/**
 * A common base class for terminals and nonterminals.
 * 
 * @author romanm
 *
 */
public abstract class Symbol {
	int subgraphRank = 0;
	public int Rank() {
		return subgraphRank;
	}
}