package heap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.stringtemplate.v4.ST;

import bgu.cs.util.STGLoader;
import bgu.cs.util.STHierarchyRenderer;
import bgu.cs.util.rel.HashRel2;
import bgu.cs.util.rel.Rel2;
import gp.Domain;
import heap.Var.VarRole;

/**
 * A domain for heap-manipulating programs.
 * 
 * @author romanm
 */
public class HeapDomain implements Domain<Store, BasicStmt, Condition> {
	public final Set<Field> fields = new LinkedHashSet<>();

	public final Collection<Var> vars;
	public final List<RefVar> refVars = new ArrayList<>();
	public final List<RefVar> refTemps = new ArrayList<>();
	public final List<RefVar> refArgs = new ArrayList<>();

	public final Collection<RefType> refTypes;
	public final Collection<Type> types = new LinkedHashSet<>();

	public final Rel2<Type, Var> typeToVar = new HashRel2<>();

	protected Collection<BasicStmt> actions = new ArrayList<>();

	protected STGLoader templates = new STGLoader(HeapDomain.class);
	protected STHierarchyRenderer renderer = new STHierarchyRenderer(templates);

	@Override
	public String name() {
		return "HeapDomain";
	}

	@Override
	public boolean test(Condition c, Store state) {
		return c.holds(state);
	}

	public static HeapDomain fromVarsAndTypes(Collection<Var> vars, Collection<RefType> refTypes) {
		HeapDomain result = new HeapDomain(vars, refTypes);
		return result;
	}

	protected HeapDomain(Collection<Var> vars, Collection<RefType> refTypes) {
		this.vars = vars;
		this.refTypes = refTypes;
		for (RefType type : refTypes) {
			fields.addAll(type.fields);
		}
		types.addAll(refTypes);
		types.add(IntType.v);

		for (Var v : vars) {
			typeToVar.add(v.getType(), v);
			if (v instanceof RefVar) {
				RefVar refVar = (RefVar) v;
				refVars.add(refVar);
				if (refVar.role == VarRole.ARG) {
					refArgs.add(refVar);
				}
				if (refVar.role == VarRole.TEMP) {
					refTemps.add(refVar);
				}
			}
		}

		actions = generateActions(vars, fields);
	}

	// TODO: make this call explicit and add a flag for allocation statements.
	public Collection<BasicStmt> generateActions(Collection<Var> vars, Collection<Field> fields) {
		Collection<BasicStmt> result = new ArrayList<>();

		// Generate variable-to-variable assignments.
		for (Var lhs : vars) {
			if (lhs.readonly) {
				continue;
			}
			for (Var rhs : typeToVar.select1(lhs.getType())) {
				if (lhs != rhs) {
					Copy stmt = new Copy(lhs, rhs);
					result.add(stmt);
				}
			}
		}

		for (RefVar lhs : refVars) {
			if (!lhs.readonly) {
				// lhs = new T()
				result.add(new CopyNewExpr(lhs, lhs.getType()));
				// lhs = null
				result.add(new CopyNullExpr(lhs));
			}

			for (Field f : lhs.getType().fields) {
				if (f instanceof RefField) {
					// lhs.f = null
					result.add(new StoreFieldNullExpr(lhs, (RefField) f));
				}
				for (Var rhs : typeToVar.select1(f.dstType)) {
					// lhs.f = rhs
					result.add(new StoreField(lhs, f, rhs));
				}
			}

			if (!lhs.readonly) {
				for (Field f : fields) {
					if (f.dstType == lhs.getType()) {
						for (Var rhs : typeToVar.select1(f.srcType)) {
							// lhs = rhs.f
							result.add(new LoadField(lhs, (RefVar) rhs, f));
						}
					}
				}
			}
		}

		return result;
	}

	@Override
	public String toString() {
		ST template = templates.load("HeapDomain");
		for (Type type : types) {
			if (type instanceof RefType) {
				ST refTypeTemplate = templates.load("RefType");
				refTypeTemplate.add("name", type.name);
				RefType refType = (RefType) type;
				for (Field f : refType.fields) {
					ST fieldTemplate = templates.load("Field");
					fieldTemplate.add("name", f.name);
					fieldTemplate.add("dstType", f.dstType.name);
					if (f.ghost) {
						fieldTemplate.add("ghost", "true");
					}
					refTypeTemplate.add("fields", fieldTemplate.render());
				}
				template.add("types", refTypeTemplate.render());
			} else {
				template.add("types", type);
			}
		}
		for (Var v : vars) {
			template.add("vars", renderer.render(v));
		}
		// template.add("vars", vars);
		template.add("actions", actions);
		return template.render();

		// TODO: make the auto-renderer work.
		// STHierarchyRenderer renderer = new STHierarchyRenderer(HeapDomain.class,
		// "HeapDomain");
		// return renderer.render(this);
	}
}