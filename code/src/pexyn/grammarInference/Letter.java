package pexyn.grammarInference;

import pexyn.Semantics.Store;

/**
 * A letter that can appear in words of a context-free grammar.
 * 
 * @author romanm
 */
public abstract class Letter {
	public Store state = null;
	
	@Override
	abstract public String toString();

	@Override
	abstract public boolean equals(Object obj);
}