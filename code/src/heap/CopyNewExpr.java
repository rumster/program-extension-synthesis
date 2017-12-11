package heap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import grammar.Node;
import heap.Store.Obj;

/**
 * An assignment of a newly-allocated object into a variable.
 * 
 * @author romanm
 */
public class CopyNewExpr extends AssignStmt {
	public final RefVar lhs;
	public final RefType type;

	public CopyNewExpr(RefVar lhs, RefType type) {
		super(lhs, new NewExpr(type));
		assert type == lhs.getType();
		this.lhs = lhs;
		this.type = type;
	}

	@Override
	public boolean enabled(Store s) {
		return !s.freeObjects.isEmpty();
	}

	@Override
	public Collection<Store> apply(Store s) {
		if (s.freeObjects.isEmpty()) {
			Store result = Store.error("An attempt to allocate with an empty set of free obejcts!");
			return List.of(result);
		} else {
			ArrayList<Store> result = new ArrayList<>(s.freeObjects.size());
			for (Obj freeObj : s.freeObjects) {
				Store nextStore = s.assign(lhs, freeObj);
				result.add(nextStore);
			}
			return result;
		}
	}

	@Override
	public Node clone(List<Node> args) {
		NewExpr expr = (NewExpr) args.get(1);
		return new CopyNewExpr((RefVar) args.get(0), expr.getType());
	}
	
	@Override
	public String toString() {
		return lhs.name + "=new " + type.name;
	}
}