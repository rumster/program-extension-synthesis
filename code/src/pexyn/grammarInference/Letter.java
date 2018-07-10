package pexyn.grammarInference;

/**
 * A letter that can appear in words of a context-free grammar.
 * 
 * @author romanm
 */
public abstract class Letter {
	@Override
	abstract public String toString();

	@Override
	abstract public boolean equals(Object obj);
}