package heap.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import bgu.cs.util.Union2;
import gp.Example;
import heap.BasicStmt;
import heap.Field;
import heap.HeapDomain;
import heap.HeapProblem;
import heap.IntField;
import heap.IntType;
import heap.IntVal;
import heap.IntVar;
import heap.Obj;
import heap.RefField;
import heap.RefType;
import heap.RefVar;
import heap.Store;
import heap.Type;
import heap.Val;
import heap.Var;
import heap.Var.VarRole;

/**
 * Constructs a {@link HeapProblem} from an abstract syntax tree.
 * 
 * @author romanm
 *
 */
public class ProblemCompiler {
	protected final ASTProblem root;
	protected Map<String, Type> nameToType = new HashMap<>();
	protected Map<String, RefType> nameToRefType = new HashMap<>();
	protected Map<String, Type> nameToField = new HashMap<>();
	protected ASTFun funAST;
	protected Map<String, Var> nameToVar = new HashMap<>();
	protected List<Var> vars = new ArrayList<>();

	public ProblemCompiler(ASTProblem root) {
		this.root = root;
	}

	public HeapProblem compile() {
		new TypeBuilder().apply();
		new FieldBuilder().apply();
		new FunDefFinder().apply();
		new VarBuilder(VarRole.ARG, false).build(funAST.inputArgs);
		new VarBuilder(VarRole.ARG, true).build(funAST.outputArgs);
		new VarBuilder(VarRole.TEMP, false).build(funAST.temps);
		HeapDomain domain = HeapDomain.fromVarsAndTypes(vars, nameToRefType.values());

		HeapProblem result = new HeapProblem(funAST.name, domain);
		int exampleId = 0;
		for (ASTExample exampleAST : funAST.examples) {
			Example<Store, BasicStmt> example = new ExampleBuilder(exampleAST, exampleId).build();
			result.addExample(example);
			++exampleId;
		}
		return result;
	}

	/**
	 * Constructs reference type and associates a {@link Type} with each name.
	 * 
	 * @author romanm
	 */
	public class TypeBuilder extends Visitor {
		public void apply() {
			root.accept(this);
			nameToType.putAll(nameToRefType);
			nameToType.put(AST.INT_TYPE_NAME, IntType.v);
		}

		public void visit(ASTRefType n) {
			if (nameToRefType.containsKey(n.name)) {
				throw new SemanticError("Attempt to re-define type " + n.name, n);
			}
			nameToRefType.put(n.name, new RefType(n.name));
		}
	}

	/**
	 * Populates a reference type with its fields.
	 * 
	 * @author romanm
	 */
	public class FieldBuilder extends Visitor {
		public void apply() {
			root.accept(this);
		}

		public void visit(ASTRefType n) {
			RefType type = nameToRefType.get(n.name);
			for (ASTField fieldAST : n.fields) {
				Type fieldType = nameToType.get(fieldAST.typeName);
				if (fieldType == null) {
					throw new SemanticError("Field " + fieldAST.name + " of type " + n.name
							+ " refers to undefined type " + fieldAST.typeName, fieldAST);
				}
				if (fieldType == IntType.v) {
					IntField field = new IntField(fieldAST.name, type, fieldAST.ghost);
					type.add(field);
				} else {
					RefField field = new RefField(fieldAST.name, type, (RefType) fieldType, fieldAST.ghost);
					type.add(field);
				}
			}
		}
	}

	/**
	 * Finds the function definition and checks that there is only one such
	 * definition.
	 * 
	 * @author romanm
	 */
	public class FunDefFinder extends Visitor {
		public void apply() {
			assert funAST == null : "Don't call this method twice!";
			root.accept(this);
			if (funAST == null) {
				throw new SemanticError("Unable to find a function definition!");
			}
		}

		public void visit(ASTFun n) {
			if (funAST != null) {
				throw new SemanticError("Attempt to define a second function " + n.name, n);
			}
			funAST = n;
		}
	}

	/**
	 * Populates the list of variables and mapping from names to variables from a
	 * list of variable nodes.
	 * 
	 * @author romanm
	 */
	public class VarBuilder extends Visitor {
		private final VarRole role;
		private final boolean out;

		public VarBuilder(VarRole role, boolean out) {
			this.role = role;
			this.out = out;
		}

		public void build(List<ASTVar> varsAST) {
			for (ASTVar n : varsAST) {
				n.accept(this);
			}
		}

		public void visit(ASTVar n) {
			Type type = nameToType.get(n.type);
			if (type == IntType.v) {
				IntVar var = new IntVar(n.name, role, out, n.readonly);
				addVar(var);
			} else {
				assert type instanceof RefType;
				RefVar var = new RefVar(n.name, (RefType) type, role, out, n.readonly);
				addVar(var);
			}
		}

		private void addVar(Var var) {
			vars.add(var);
			Var oldVar = nameToVar.put(var.name, var);
			if (oldVar != null) {
				throw new SemanticError("Variable " + var.name + " is defined in more than one context!");
			}
		}
	}

	/**
	 * Converts an {@link ASTExample} into an {@link Example}.
	 * 
	 * @author romanm
	 */
	public class ExampleBuilder {
		private final ASTExample exampleAST;
		private final int exampleId;
		private final Map<String, RefType> objNameToRefType = new HashMap<>();
		private final Map<String, Obj> objNameToObj = new HashMap<>();

		public ExampleBuilder(ASTExample exampleAST, int exampleId) {
			this.exampleAST = exampleAST;
			this.exampleId = exampleId;
		}

		public Example<Store, BasicStmt> build() {
			ObjFinder objFinder = new ObjFinder();
			Set<String> inputObjNames = objFinder.find(exampleAST.input());
			inputObjNames.remove(AST.NULL_VAL_NAME);
			Set<String> goalObjNames = objFinder.find(exampleAST.goal());
			goalObjNames.remove(AST.NULL_VAL_NAME);
			Set<String> allObjNames = new HashSet<>();
			allObjNames.addAll(inputObjNames);
			allObjNames.addAll(goalObjNames);

			inferObjectTypes();

			// Construct all objects and populate input/goal object sets.
			Set<Obj> inputObjs = new HashSet<>();
			Set<Obj> goalObjs = new HashSet<>();
			objNameToObj.put(AST.NULL_VAL_NAME, Obj.NULL);
			for (String objName : allObjNames) {
				RefType type = objNameToRefType.get(objName);
				if (type != null) {
					Obj obj = new Obj(type);
					objNameToObj.put(objName, obj);
					if (inputObjNames.contains(objName)) {
						inputObjs.add(obj);
					}
					if (goalObjNames.contains(objName)) {
						goalObjs.add(obj);
					}
				} else {
					throw new SemanticError("Unable to type object " + objName);
				}
			}
			Set<Obj> freeInputObjs = new HashSet<>(goalObjs);
			freeInputObjs.removeAll(inputObjs);

			List<Union2<Store, BasicStmt>> stores = new ArrayList<>(exampleAST.steps.size());
			for (ASTStep stepAST : exampleAST.steps) {
				if (stepAST instanceof ASTStore) {
					ASTStore storeAST = (ASTStore) stepAST;

					Set<String> storeObjNames = objFinder.find(storeAST);
					storeObjNames.remove(AST.NULL_VAL_NAME);
					Set<Obj> storeObjs;

					if (storeAST == exampleAST.steps.get(0)) {
						storeObjs = inputObjs;
					} else {
						storeObjs = new HashSet<>();
						for (String objName : storeObjNames) {
							Obj obj = objNameToObj.get(objName);
							storeObjs.add(obj);
						}
					}
					Store store = new StoreBuilder(storeObjs, freeInputObjs, storeAST).build();
					stores.add(Union2.ofT1(store));
				}
				else {
					
				}
			}
			return new Example<Store, BasicStmt>(stores, exampleId);
		}

		private void inferObjectTypes() {
			ObjTypeAssigner typeAssigner = new ObjTypeAssigner();
			boolean typeChange = true;
			while (typeChange) {
				typeChange = false;
				for (ASTStep stepAST : exampleAST.steps) {
					if (stepAST instanceof ASTStore) {
						ASTStore storeAST = (ASTStore) stepAST;
						typeChange |= typeAssigner.infer(storeAST);
					}
				}
			}
		}

		/**
		 * Build a store from an AST.
		 * 
		 * @author romanm
		 */
		public class StoreBuilder extends Visitor {
			private final Set<Obj> objs;
			private final Set<Obj> freeObjs;
			private final ASTStore ast;
			private final Map<Var, Val> env = new HashMap<>();
			private final Map<Obj, Map<Field, Val>> heap = new HashMap<>();

			public StoreBuilder(Set<Obj> objs, Set<Obj> freeObjs, ASTStore ast) {
				this.objs = objs;
				this.freeObjs = freeObjs;
				this.ast = ast;
			}

			public Store build() {
				ast.accept(this);
				return new Store(objs, freeObjs, env, heap);
			}

			public void visit(ASTRefFieldVal n) {
				Obj obj = objNameToObj.get(n.objName);
				Optional<Field> maybeField = obj.type.findField(n.fieldName);
				assert maybeField.isPresent();
				RefField refField = (RefField) maybeField.get();
				Map<Field, Val> fields = heap.get(obj);
				if (fields == null) {
					fields = new HashMap<>();
					heap.put(obj, fields);
				}
				fields.put(refField, objNameToObj.get(n.val));
			}

			public void visit(ASTIntFieldVal n) {
				Obj obj = objNameToObj.get(n.objName);
				Optional<Field> maybeField = obj.type.findField(n.fieldName);
				assert maybeField.isPresent();
				IntField refField = (IntField) maybeField.get();
				Map<Field, Val> fields = heap.get(obj);
				if (fields == null) {
					fields = new HashMap<>();
					heap.put(obj, fields);
				}
				fields.put(refField, new IntVal(n.val));
			}

			public void visit(ASTRefVarVal n) {
				Var var = nameToVar.get(n.varName);
				Obj obj = objNameToObj.get(n.val);
				env.put(var, obj);
			}

			public void visit(ASTIntVarVal n) {
				Var var = nameToVar.get(n.varName);
				env.put(var, new IntVal(n.val));
			}
		}

		/**
		 * Finds the list of object names appearing in an example AST.
		 * 
		 * @author romanm
		 */
		public class ObjFinder extends Visitor {
			Set<String> result;

			public Set<String> find(ASTStore store) {
				result = new HashSet<>();
				store.accept(this);
				return result;
			}

			public void visit(ASTRefFieldVal n) {
				result.add(n.objName);
				result.add(n.val);
			}

			public void visit(ASTIntFieldVal n) {
				result.add(n.objName);
			}

			public void visit(ASTRefVarVal n) {
				result.add(n.val);
			}
		}

		/**
		 * Assigns types to objects based on the specified equality constraints.
		 * 
		 * @author romanm
		 */
		public class ObjTypeAssigner extends Visitor {
			boolean change;

			public boolean infer(ASTStore n) {
				change = false;
				n.accept(this);
				return change;
			}

			public void visit(ASTRefVarVal n) {
				Var var = nameToVar.get(n.varName);
				if (var == null) {
					throw new SemanticError(
							"Reference to undeclared variable " + n.varName + " as a reference to object " + n.val);
				}
				Type type = var.getType();
				if (type instanceof RefType) {
					RefType refType = (RefType) type;
					updateType(n.val, refType);
				} else {
					throw new SemanticError("Non-reference variable " + var.name
							+ " is used as a reference variable for object " + n.val);
				}
			}

			public void visit(ASTRefFieldVal n) {
				RefType objRefType = objNameToRefType.get(n.objName);
				if (objRefType != null) {
					Optional<Field> maybeField = objRefType.findField(n.fieldName);
					if (!maybeField.isPresent()) {
						throw new SemanticError(
								"Field " + n.fieldName + " is not defined in type " + objRefType.getName());
					}
					Field field = maybeField.get();
					if (field instanceof RefField) {
						RefField refField = (RefField) field;
						updateType(n.val, refField.getDstType());

					} else {
						throw new SemanticError("Non-reference field " + n.fieldName + " is used as a reference field");
					}
				}
			}

			private void updateType(String objName, RefType type) {
				if (objName == AST.NULL_VAL_NAME) {
					return;
				}
				RefType oldType = objNameToRefType.put(objName, type);
				if (oldType == null) {
					change = true;
				} else if (oldType != type) {
					throw new SemanticError("Attempt to re-define the type of object " + objName);
				}
			}
		}
	}
}