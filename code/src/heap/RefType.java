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
	public final Set<Field> fields = new HashSet<>();

	/**
	 * Indicates whether additional fields may be added. When the type is accessed,
	 * it automatically becomes immutable.
	 */
	private boolean mutable = true;

	public RefType(String name) {
		super(name);
	}

	public void add(Field field) {
		assert field.srcType == this;
		assert mutable;
		fields.add(field);
	}

	@Override
	public int hashCode() {
		mutable = false;
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((fields == null) ? 0 : fields.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		mutable = false;
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
		mutable = false;
		PWhileVisitor whileVisitor = (PWhileVisitor) v;
		whileVisitor.visit(this);
	}
}