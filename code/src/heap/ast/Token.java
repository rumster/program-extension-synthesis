package heap.ast;

import java_cup.runtime.Symbol;

/**
 * A lexical token.
 * 
 * @author romanm
 */
public class Token extends Symbol {
	public final int line;
	public final int column;
	public final String text;

	public Token(int id, String text, int line, int column) {
		super(id);
		this.line = line;
		this.column = column;
		this.text = text.intern();
	}
}