package heap.ast;

import java.util.HashMap;
import java.util.Map;

import heap.RefType;
import heap.Store.Obj;

public class ASTGen {
	private Map<String, Obj> nameToObj = new HashMap<>();
	private Map<String, RefType> nameToRefType = new HashMap<>();

	public RefType getRefType(String name) {
		RefType result = nameToRefType.get(name);
		if (result == null) {
			result = new RefType(name);
			nameToRefType.put(name, result);
		}
		return result;
	}

	// public Obj getObj(String name) {
	// Obj result = nameToObj.get(name);
	// if (result == null) {
	// result = new Obj();
	// }
	// return result;
	// }
}