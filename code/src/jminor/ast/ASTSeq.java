package jminor.ast;

/**
 * A node for a sequence statement.
 * 
 * @author romanm
 */
public class ASTSeq extends ASTStmt {
	public final ASTStmt first;
	public final ASTStmt second;

	public ASTSeq(ASTStmt first, ASTStmt second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}