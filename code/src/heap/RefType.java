package heap;

import java.util.HashSet;
import java.util.Set;

import grammar.Visitor;

/**
 * Represents an object type.
 * 
 * @author romanm
 */
public class RefType extends Type {
	public Set<Field> fields = new HashSet<>();

	public RefType(String name) {
		super(name);
	}

	public void add(Field field) {
		assert field.srcType == this;
		fields.add(field);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((fields == null) ? 0 : fields.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof RefType))
			return false;
		RefType other = (RefType) obj;
		if (fields == null) {
			if (other.fields != null) {
				return false;
			}
		} else if (!fields.equals(other.fields))
			return false;
		return true;
	}

	@Override
	public void accept(Visitor v) {
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}
	
	@Override
	public String toString() {
		return name;
	}
}