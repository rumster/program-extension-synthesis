package pexyn.grammarInference;

import java.util.Collection;
import java.util.function.Function;

/**
 * A function that transforms an input grammar into an output grammar. As a The
 * language of the output grammar should contain the language of the input
 * grammar.
 * 
 * @author romanm
 *
 * @param The
 *            type of an alphabet letter.
 */
public interface Transform extends Function<Grammar, Grammar> {
	@Override
	public Grammar apply(Grammar g);

	public Collection<Nonterminal> changed();
}