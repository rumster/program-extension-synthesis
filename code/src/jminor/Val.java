package jminor;

import bgu.cs.util.treeGrammar.Token;

/**
 * The base class of values.
 * 
 * @author romanm
 */
public abstract class Val extends Token {
	public abstract Type type();
}