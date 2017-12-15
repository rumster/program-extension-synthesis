package heap;

import java.util.HashSet;
import java.util.Optional;
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

	public Optional<Field> findField(String name) {
		for (Field f : fields) {
			if (f.name.equals(name)) {
				return Optional.of(f);
			}
		}
		return Optional.empty();
	}

	public void add(Field field) {
		assert field.srcType == this;
		assert mutable;
		if (findField(field.name).isPresent()) {
			assert findField(field.name).get() == field;
		} else {
			fields.add(field);
		}
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
	
	@Override
	public String toString() {
		return name;
	}
}