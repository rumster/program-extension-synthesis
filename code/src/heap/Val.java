package heap;

import grammar.Token;

/**
 * The base class of values.
 * 
 * @author romanm
 */
public abstract class Val extends Token {
	public static final Val TOP = new Val() {
	};

	public String getName() {
		throw new Error("shouldnt be called");
	};
}