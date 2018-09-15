package pexyn.grammarInference;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jminor.Stmt;
import pexyn.Semantics.Store;

/**
 * A common base class for terminals and nonterminals.
 * 
 * @author romanm
 *
 */
public abstract class Symbol {
	public Set<Store> states = new HashSet<>();
	
	public Set<Store> states(){
		return states;
	}
	
	int subgraphRank = 0;
	public int Rank() {
		return subgraphRank;
	}
	abstract Map<Stmt, Set<Store>> FirstStmts();
}