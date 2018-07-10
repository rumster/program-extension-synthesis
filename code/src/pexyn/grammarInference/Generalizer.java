package pexyn.grammarInference;

import java.util.List;

/**
 * A base class for algorithms that process a sequence words incrementally and
 * produces a context-free grammar whose language includes those words.
 * 
 * @author romanm
 *
 * @param The
 *            type of an alphabet letter.
 */
public abstract class Generalizer {
	protected Grammar grammar;

	/**
	 * Constructs a generalizer with an initial empty-language grammar.
	 */
	public Generalizer() {
		grammar = new Grammar();
	}

	public void clear() {
		grammar.clear();
	}

	/**
	 * Returns the grammar produced so far.
	 */
	public Grammar current() {
		return grammar;
	}

	/**
	 * Updates the grammar with an entire word.
	 * 
	 * @param word
	 *            to process into the grammar
	 * @return - the updated grammar
	 */
	public Grammar addExample(List<? extends Letter> word) {
		append(word);
		endWord();
		return current();
	}

	/**
	 * Appends a list of letters to the most recent word that is being
	 * considered and updates the grammar accordingly.
	 */
	public abstract void append(List<? extends Letter> word);

	/**
	 * Signals that the most recently considered word has ended. Subsequent
	 * calls to append will apply to a new word.
	 */
	public abstract void endWord();

}