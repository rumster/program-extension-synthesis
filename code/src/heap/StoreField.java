package heap;

import java.util.Collection;
import java.util.List;

import grammar.Node;
import heap.Store.Obj;

/**
 * An assignment of a variable into a field.
 * 
 * @author romanm
 */
public class StoreField extends AssignStmt {
	public final Var rhs;
	public final RefVar lhs;
	public final Field field;

	public StoreField(RefVar lhs, Field field, Var rhs) {
		super(new DerefExpr(lhs, field), rhs);
		assert field.srcType == lhs.type && field.dstType == rhs.type;
		this.lhs = lhs;
		this.rhs = rhs;
		this.field = field;
	}

	@Override
	public boolean enabled(Store s) {
		return s.isInitialized(rhs) && s.isInitialized(lhs) && s.eval(lhs) != Obj.NULL;
	}

	@Override
	public Collection<Store> apply(Store s) {
		Store result;
		if (!s.isInitialized(rhs)) {
			result = Store.error("An attempt to copy from an uninitialized variable: " + rhs.name);
		} else if (!s.isInitialized(lhs)) {
			result = Store.error("An attempt to load into the field of an uninitialized variable: " + lhs.name);
		} else if (s.eval(lhs) == Obj.NULL) {
			result = Store.error("An attempt to load into the field of a null variable: " + lhs.name);
		} else {
			result = s.assign(lhs, field, s.eval(rhs));
		}
		return List.of(result);
	}

	@Override
	public Node clone(List<Node> args) {
		DerefExpr expr = (DerefExpr) args.get(0);
		return new StoreField((RefVar) expr.getLhs(), expr.getField(), (Var) args.get(1));
	}
	
	@Override
	public String toString() {
		return lhs.name + "." + field.name + "=" + rhs.name;
	}

}