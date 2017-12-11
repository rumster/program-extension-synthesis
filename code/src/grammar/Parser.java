package grammar;

import java.util.List;
import java.util.Optional;

/**
 * An algorithm for recognizing words in a context-free grammar and producing a
 * derivation tree.
 * 
 * @author romanm
 */
public interface Parser {
	/**
	 * Parses the input word.
	 *
	 * @param word
	 *            A sequence of tokens.
	 * @return A derivation tree or empty if the word is not in the language of the
	 *         grammar.
	 */
	public Optional<Node> parse(List<Token> word);
}