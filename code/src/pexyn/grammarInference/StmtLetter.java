package pexyn.grammarInference;

import jminor.Stmt;

public class StmtLetter extends Letter {
	final Stmt cmd;

	public StmtLetter(Stmt cmd) {
		super();
		this.cmd = cmd;
	}

	@Override
	public String toString() {
		return cmd.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StmtLetter other = (StmtLetter) obj;
		return (cmd.equals(other.cmd));
	}



}