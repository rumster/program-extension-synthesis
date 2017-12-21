package heap;

import java.util.Collection;
import java.util.List;

import grammar.Node;

/**
 * An assignment of a field value into a variable.
 * 
 * @author romanm
 */
public class LoadField extends AssignStmt {
	public final Var lhs;
	public final RefVar rhs;
	public final Field field;

	public LoadField(Var lhs, RefVar rhs, Field field) {
		super(lhs, new DerefExpr(rhs, field));
		assert field.srcType == rhs.getType() && field.dstType == lhs.getType();
		this.lhs = lhs;
		this.rhs = rhs;
		this.field = field;
	}

	@Override
	public boolean enabled(Store s) {
		return s.isInitialized(rhs) && s.isInitialized(s.eval(rhs), field) && s.eval(rhs) != Obj.NULL;
	}

	@Override
	public Collection<Store> apply(Store s) {
		Store result;
		if (!s.isInitialized(rhs)) {
			result = Store.error("An attempt to load via an uninitialized variable: " + rhs.name);
		} else if (s.eval(rhs) == Obj.NULL) {
			result = Store.error("An attempt to load via a null variable: " + rhs.name);
		} else {
			result = s.assign(lhs, s.eval(rhs, field));
		}
		if (result.containsGarbage()) {
			return List.of();
		} else {
			return List.of(result);
		}
	}

	@Override
	public Node clone(List<Node> args) {
		DerefExpr expr = (DerefExpr) args.get(1);
		return new LoadField((Var) args.get(0), (RefVar) expr.getLhs(), expr.getField());
	}

	@Override
	public String toString() {
		return lhs.name + "=" + rhs.name + "." + field.name;
	}
}