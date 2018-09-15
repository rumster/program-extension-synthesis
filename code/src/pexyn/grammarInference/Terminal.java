package pexyn.grammarInference;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jminor.Stmt;
import pexyn.Semantics.Store;

/**
 * A terminal represents a letter along with its instances in different words.
 * 
 * @author romanm
 */
public class Terminal extends Symbol {
	public final Letter id;

	public Terminal(Letter id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Terminal other = (Terminal) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return id.toString();
	}

	@Override
	Map<Stmt, Set<Store>> FirstStmts() {
		var map = new HashMap<Stmt, Set<Store>>();
		var stmt = ((StmtLetter)id).cmd;
		map.put(stmt, states);
		return map;
	}
}