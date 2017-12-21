package heap;

import java.util.Collection;
import java.util.List;

import grammar.Node;

/**
 * An assignment between two variables of the same type.
 * 
 * @author romanm
 */
public class Copy extends AssignStmt {
	public final Var lhs;
	public final Var rhs;

	public Copy(Var lhs, Var rhs) {
		super(lhs, rhs);
		assert lhs.getType() == rhs.getType();
		this.lhs = lhs;
		this.rhs = rhs;
	}

	@Override
	public boolean enabled(Store s) {
		return s.isInitialized(rhs);
	}

	@Override
	public Collection<Store> apply(Store s) {
		Store result;
		if (!s.isInitialized(rhs)) {
			result = Store.error("An attempt to copy from an uninitialized variable: " + rhs.name);
		} else {
			result = s.assign(lhs, s.eval(rhs));
		}
		if (result.containsGarbage()) {
			return List.of();
		} else {
			return List.of(result);
		}
	}

	@Override
	public Copy clone(List<Node> args) {
		return new Copy((Var) args.get(0), (Var) args.get(1));
	}

	@Override
	public String toString() {
		return lhs.name + "=" + rhs.name;
	}
}