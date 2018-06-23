package jminor.ast;

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
		super(id, text);
		this.line = line;
		this.column = column;
		this.text = text.intern();
	}

	public Token(int id, Integer val, int line, int column) {
		super(id, val);
		this.line = line;
		this.column = column;
		this.text = val.toString();
	}

	public Token(int id, boolean val, int line, int column) {
		super(id, val);
		this.line = line;
		this.column = column;
		this.text = "" + val;
	}
}