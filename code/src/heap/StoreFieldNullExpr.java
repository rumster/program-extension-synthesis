package heap;

import java.util.Collection;
import java.util.List;

import grammar.Node;

/**
 * An assignment of null into a field.
 * 
 * @author romanm
 */
public class StoreFieldNullExpr extends AssignStmt {
	public final RefVar lhs;
	public final RefField field;

	public StoreFieldNullExpr(RefVar lhs, RefField field) {
		super(new DerefExpr(lhs, field), NullExpr.v);
		assert field.srcType == lhs.getType();
		this.lhs = lhs;
		this.field = field;
	}

	@Override
	public boolean enabled(Store s) {
		return s.isInitialized(lhs) && s.eval(lhs) != Obj.NULL;
	}

	@Override
	public Collection<Store> apply(Store s) {
		Store result;
		if (!s.isInitialized(lhs)) {
			result = Store.error("An attempt to load into the field of an uninitialized variable: " + lhs.name);
		} else if (s.eval(lhs) == Obj.NULL) {
			result = Store.error("An attempt to load into the field of a null variable: " + lhs.name);
		} else {
			result = s.assign(lhs, field, Obj.NULL);
		}
		if (result.containsGarbage()) {
			return List.of();
		} else {
			return List.of(result);
		}
	}

	@Override
	public Node clone(List<Node> args) {
		DerefExpr expr = (DerefExpr) args.get(0);
		return new StoreFieldNullExpr((RefVar) expr.getLhs(), (RefField) expr.getField());
	}

	@Override
	public String toString() {
		return lhs.name + "." + field.name + "=null";
	}

}