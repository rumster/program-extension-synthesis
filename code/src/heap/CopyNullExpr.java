package heap;

import java.util.Collection;
import java.util.List;

import grammar.Node;

/**
 * An assignment of null into a variable.
 * 
 * @author romanm
 */
public class CopyNullExpr extends AssignStmt {
	public final RefVar lhs;

	public CopyNullExpr(RefVar lhs) {
		super(lhs, NullExpr.v);
		this.lhs = lhs;
	}

	@Override
	public boolean enabled(Store s) {
		return true;
	}

	@Override
	public Collection<Store> apply(Store s) {
		Store result = s.assign(lhs, Obj.NULL);
		if (result.containsGarbage()) {
			return List.of();
		} else {
			return List.of(result);
		}
	}

	@Override
	public CopyNullExpr clone(List<Node> args) {
		return new CopyNullExpr((RefVar) args.get(0));
	}

	@Override
	public String toString() {
		return lhs.name + "=null";
	}
}