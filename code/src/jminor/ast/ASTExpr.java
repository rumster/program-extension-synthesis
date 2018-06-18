package jminor.ast;

import java.util.Optional;

import jminor.Type;

/**
 * The base class of expression nodes.
 * 
 * @author romanm
 */
public abstract class ASTExpr extends AST {
	/**
	 * The type inferred for this expression.
	 */
	private Type type;

	public Optional<Type> type() {
		return Optional.ofNullable(type);
	}

	public void setType(Type type) {
		assert this.type == null;
		this.type = type;
	}
}