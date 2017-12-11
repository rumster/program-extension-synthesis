package treedoc;

import java.util.List;

import grammar.Grammar;

/**
 * A machine-learning algorithm to learn a context-free grammar from a list of
 * positive examples.
 * 
 * @author romanm
 */
public interface GrammarLearner {
	public Grammar infer(List<String> examples);
}