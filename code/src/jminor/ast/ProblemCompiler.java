package jminor.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import bgu.cs.util.Union2;
import jminor.AndExpr;
import jminor.AssignStmt;
import jminor.BoolType;
import jminor.DerefExpr;
import jminor.EqExpr;
import jminor.Expr;
import jminor.Field;
import jminor.JminorSemantics;
import jminor.JminorProblem;
import jminor.IfStmt;
import jminor.IntBinOp;
import jminor.IntBinOpExpr;
import jminor.IntField;
import jminor.IntType;
import jminor.IntVal;
import jminor.IntVar;
import jminor.NotExpr;
import jminor.NullExpr;
import jminor.Obj;
import jminor.OrExpr;
import jminor.RefField;
import jminor.RefType;
import jminor.RefVar;
import jminor.RetStmt;
import jminor.SeqStmt;
import jminor.Stmt;
import jminor.JmStore;
import jminor.Type;
import jminor.Val;
import jminor.ValExpr;
import jminor.Var;
import jminor.VarExpr;
import jminor.WhileStmt;
import jminor.Var.VarRole;
import pexyn.Example;

/**
 * Constructs a {@link JminorProblem} from an abstract syntax tree.
 * 
 * @author romanm
 */
public class ProblemCompiler {
	protected final ASTProblem root;
	protected Map<String, Type> nameToType = new HashMap<>();
	protected Map<String, RefType> nameToRefType = new HashMap<>();
	protected ASTFun funAST;
	protected Map<String, Var> nameToVar = new HashMap<>();
	protected List<Var> vars = new ArrayList<>();

	public ProblemCompiler(ASTProblem root) {
		this.root = root;
	}

	public JminorProblem compile() {
		new RefTypeBuilder().apply();
		new FieldBuilder().apply();
		new FunDefFinder().apply();
		new TypedVarBuilder(VarRole.ARG, false).build(funAST.inputArgs);
		new TypedVarBuilder(VarRole.ARG, true).build(funAST.outputArgs);
		new TypedVarBuilder(VarRole.TEMP, false).build(funAST.temps);
		JminorSemantics semantics = JminorSemantics.fromVarsAndTypes(vars, nameToRefType.values());

		Optional<Stmt> optProg;
		if (funAST.optProg != null) {
			var prog = funAST.optProg;
			var stmtBuilder = new StmtBuilder();
			var stmt = stmtBuilder.build(prog);
			optProg = Optional.of(stmt);
		} else {
			optProg = Optional.empty();
		}

		var inputVars = new ArrayList<Var>();
		for (var inputArgAST : funAST.inputArgs) {
			var inputVar = nameToVar.get(inputArgAST.name);
			inputVars.add(inputVar);
		}
		var outputVars = new ArrayList<Var>();
		for (var outputArgAST : funAST.outputArgs) {
			var outputVar = nameToVar.get(outputArgAST.name);
			outputVars.add(outputVar);
		}
		var tempVars = new ArrayList<Var>();
		for (var tempArgAST : funAST.temps) {
			var tempVar = nameToVar.get(tempArgAST.name);
			tempVars.add(tempVar);
		}

		JminorProblem result = new JminorProblem(funAST.name, semantics, inputVars, outputVars, tempVars, optProg);

		int exampleId = 0;
		for (ASTExample exampleAST : funAST.examples) {
			Example<JmStore, Stmt> example = new ExampleBuilder(exampleAST, exampleId).build();
			result.addExample(example);
			++exampleId;
		}
		return result;
	}

	/**
	 * Constructs reference types and associates a {@link Type} with each name.
	 * 
	 * @author romanm
	 */
	public class RefTypeBuilder extends Visitor {
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
			for (ASTDeclField fieldAST : n.fields) {
				Type fieldType = nameToType.get(fieldAST.typeName);
				if (fieldType == null) {
					throw new SemanticError("Field " + fieldAST.name + " of type " + n.name
							+ " refers to undefined type " + fieldAST.typeName, fieldAST);
				}
				if (type.findField(fieldAST.name).isPresent()) {
					throw new SemanticError("Field " + fieldAST.name + " already exists in type ", fieldAST);
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
				throw new SemanticError("Unable to find a function definition", root);
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
	 * list of variable nodes. Variables are assigned with types.
	 * 
	 * @author romanm
	 */
	public class TypedVarBuilder extends Visitor {
		private final VarRole role;
		private final boolean out;

		public TypedVarBuilder(VarRole role, boolean out) {
			this.role = role;
			this.out = out;
		}

		public void build(List<ASTVarDecl> varsAST) {
			for (ASTVarDecl n : varsAST) {
				n.accept(this);
			}
		}

		public void visit(ASTVarDecl n) {
			Type type = nameToType.get(n.type);
			if (type == IntType.v) {
				IntVar var = new IntVar(n.name, role, out, n.readonly);
				addVar(var, n);
			} else {
				assert type instanceof RefType;
				RefVar var = new RefVar(n.name, (RefType) type, role, out, n.readonly);
				addVar(var, n);
			}
		}

		private void addVar(Var var, ASTVarDecl context) {
			vars.add(var);
			Var oldVar = nameToVar.put(var.name, var);
			if (oldVar != null) {
				throw new SemanticError("Variable " + var.name + " is defined in more than one context", context);
			}
		}
	}

	/**
	 * Builds a statement from an AST while ensuring type-safety.
	 * 
	 * @author romanm
	 */
	public class StmtBuilder extends Visitor {
		private Stmt result;
		private Expr tmpExpr;

		public StmtBuilder() {
		}

		public Stmt build(ASTStmt stmtAST) {
			result = null;
			stmtAST.accept(this);
			return result;
		}

		public void visit(ASTVarExpr n) {
			Var v = nameToVar.get(n.varName);
			if (v == null) {
				throw new SemanticError("Unknown variable " + n.varName, n);
			}
			tmpExpr = new VarExpr(v);
			n.setType(v.getType());
		}

		public void visit(ASTNullExpr n) {
			tmpExpr = NullExpr.v;
		}

		public void visit(ASTIntBinOpExpr n) {
			n.lhs.accept(this);
			Expr lhsExpr = tmpExpr;
			n.rhs.accept(this);
			Expr rhsExpr = tmpExpr;
			Type lhsType = n.lhs.type().get();
			Type rhsType = n.lhs.type().get();
			if (lhsType != IntType.v || rhsType != IntType.v) {
				throw new SemanticError("Type error: attempt to apply integer-typed operation to non-integer operands!",
						n);
			}
			switch (n.op) {
			case PLUS:
				n.setType(IntType.v);
				break;
			case MINUS:
				n.setType(IntType.v);
				break;
			case TIMES:
				n.setType(IntType.v);
				break;
			case DIVIDE:
				n.setType(IntType.v);
				break;
			case EQ:
				n.setType(BoolType.v);
				break;
			case LT:
				n.setType(BoolType.v);
				break;
			case LEQ:
				n.setType(BoolType.v);
				break;
			case GT:
				n.setType(BoolType.v);
				break;
			case GEQ:
				n.setType(BoolType.v);
				break;
			default:
				throw new Error("Encountered unexpected operator " + n.op);
			}

			tmpExpr = new IntBinOpExpr(n.op, lhsExpr, rhsExpr);
		}

		public void visit(ASTAndExpr n) {
			n.lhs.accept(this);
			Expr lhsExpr = tmpExpr;
			n.rhs.accept(this);
			Expr rhsExpr = tmpExpr;
			Type lhsType = n.lhs.type().get();
			Type rhsType = n.lhs.type().get();
			if (lhsType != BoolType.v || rhsType != BoolType.v) {
				throw new SemanticError("Type error: attempt to apply && operator to non-Boolean operands!", n);
			}
			n.setType(BoolType.v);
			tmpExpr = new AndExpr(lhsExpr, rhsExpr);
		}

		public void visit(ASTEqExpr n) {
			n.lhs.accept(this);
			Expr lhsExpr = tmpExpr;
			n.rhs.accept(this);
			Expr rhsExpr = tmpExpr;
			Type lhsType = n.lhs.type().get();
			Type rhsType = n.lhs.type().get();
			if (lhsType != rhsType) {
				throw new SemanticError("Type error: attempt to apply == operator to different types!", n);
			}
			n.setType(BoolType.v);
			if (lhsType != rhsType) {
				tmpExpr = new IntBinOpExpr(IntBinOp.EQ, lhsExpr, rhsExpr);
			} else {
				tmpExpr = new EqExpr(lhsExpr, rhsExpr);
			}
		}

		public void visit(ASTOrExpr n) {
			n.lhs.accept(this);
			Expr lhsExpr = tmpExpr;
			n.rhs.accept(this);
			Expr rhsExpr = tmpExpr;
			Type lhsType = n.lhs.type().get();
			Type rhsType = n.lhs.type().get();
			if (lhsType != BoolType.v || rhsType != BoolType.v) {
				throw new SemanticError("Type error: attempt to apply || operator to non-Boolean operands!", n);
			}
			n.setType(BoolType.v);
			tmpExpr = new OrExpr(lhsExpr, rhsExpr);
		}

		public void visit(ASTNotExpr n) {
			n.sub.accept(this);
			Expr subExpr = tmpExpr;
			Type subType = n.sub.type().get();
			if (subType != BoolType.v) {
				throw new SemanticError("Type error: attempt to apply ! operator to a non-Boolean operand!", n);
			}
			n.setType(BoolType.v);
			tmpExpr = new NotExpr(subExpr);
		}

		public void visit(ASTIntValExpr n) {
			n.setType(IntType.v);
			tmpExpr = new ValExpr(new IntVal(n.val));
		}

		public void visit(ASTDerefExpr n) {
			if (n.lhs == ASTNullExpr.v) {
				throw new SemanticError("Semantic error: null cannot appear in a dereference expression!", n);
			}
			n.lhs.accept(this);
			Expr lhsExpr = tmpExpr;
			Type lhsType = n.lhs.type().get();
			if (!(lhsType instanceof RefType)) {
				throw new SemanticError("Type error: attempt to dereference a non-reference type!", n);
			}
			RefType lhsRefType = (RefType) lhsType;
			Optional<Field> optionField = lhsRefType.findField(n.fieldName);
			if (!optionField.isPresent()) {
				throw new SemanticError("Attempt to dereference an unknown field!", n);
			}
			Field field = optionField.get();
			n.setType(field.dstType);
			tmpExpr = new DerefExpr(lhsExpr, field);
		}

		public void visit(ASTAssign n) {
			n.lhs.accept(this);
			Expr lhsExpr = tmpExpr;
			if (lhsExpr instanceof VarExpr) {
				var lhsVar = (VarExpr) lhsExpr;
				if (lhsVar.getVar().readonly) {
					throw new SemanticError("Semantic error: left hand side of assignment is an immutable variabe!", n);
				}
			} else {
				assert lhsExpr instanceof DerefExpr;
			}

			n.rhs.accept(this);
			Expr rhsExpr = tmpExpr;
			Type lhsType = n.lhs.type().get();
			Type rhsType = n.lhs.type().get();

			if (!(n.lhs instanceof ASTVarExpr || n.lhs instanceof ASTDerefExpr)) {
				throw new SemanticError("Semantic error: left hand side of assignment is not an assignable expression!",
						n);
			}

			if (lhsType instanceof RefType && n.rhs == ASTNullExpr.v) {
				// OK
			} else if (n.rhs != ASTNullExpr.v && lhsType == rhsType) {
				// OK
			} else {
				throw new SemanticError("Type error: attempt to assign between incompatible types!", n);
			}
			result = new AssignStmt(lhsExpr, rhsExpr);
		}

		public void visit(ASTSeq n) {
			n.first.accept(this);
			Stmt first = result;
			n.second.accept(this);
			Stmt second = result;
			result = new SeqStmt(first, second);
		}

		public void visit(ASTIf n) {
			n.cond.accept(this);
			Type condType = n.cond.type().get();
			if (condType != BoolType.v) {
				throw new SemanticError("Type error: condition type is not boolean!", n);
			}
			Expr cond = tmpExpr;
			n.thenStmt.accept(this);
			Stmt thenStmt = result;
			Stmt elseStmt = null;
			if (n.elseStmt != null) {
				n.elseStmt.accept(this);
				elseStmt = result;
			}
			result = new IfStmt(cond, thenStmt, elseStmt);
		}

		public void visit(ASTWhile n) {
			n.cond.accept(this);
			Type condType = n.cond.type().get();
			if (condType != BoolType.v) {
				throw new SemanticError("Type error: condition type is not boolean!", n);
			}
			Expr cond = tmpExpr;
			n.body.accept(this);
			Stmt body = result;
			result = new WhileStmt(cond, body);
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

		public Example<JmStore, Stmt> build() {
			if (exampleAST.steps.size() == 0) {
				throw new SemanticError("Examples must not be empty!", exampleAST);
			}
			if (!(exampleAST.steps.get(0) instanceof ASTStore)) {
				throw new SemanticError("Example must start with a store clause!", exampleAST);
			}

			ObjFinder objFinder = new ObjFinder();
			Set<String> inputObjNames = objFinder.find(exampleAST.input());
			inputObjNames.remove(AST.NULL_VAL_NAME);
			// Set<String> goalObjNames = objFinder.find(exampleAST.goal());
			// goalObjNames.remove(AST.NULL_VAL_NAME);
			Set<String> allObjNames = new HashSet<>();
			allObjNames.addAll(inputObjNames);
			// allObjNames.addAll(goalObjNames);

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
					// if (goalObjNames.contains(objName)) {
					// goalObjs.add(obj);
					// }
				} else {
					throw new SemanticError("Unable to type object " + objName, exampleAST);
				}
			}
			Set<Obj> freeInputObjs = new HashSet<>(goalObjs);
			freeInputObjs.removeAll(inputObjs);

			List<Union2<JmStore, Stmt>> steps = new ArrayList<>(exampleAST.steps.size());
			var stmtBuilder = new StmtBuilder();
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
					JmStore store = new StoreBuilder(storeObjs, freeInputObjs, storeAST).build();
					steps.add(Union2.ofT1(store));
				} else {
					assert stepAST instanceof ASTStmt;
					var stmtAST = (ASTStmt) stepAST;
					var stmt = stmtBuilder.build(stmtAST);
					steps.add(Union2.ofT2(stmt));
				}
			}
			if (steps.size() > 1) {
				steps.add(Union2.ofT2(RetStmt.v));
			} else {
				// This is an input-only example so the return statement will be added later.
			}
			var result = new Example<JmStore, Stmt>(steps, exampleId);
			result.isTest = exampleAST.isTest;
			return result;
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
		 * Builds a store from an AST.
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

			public JmStore build() {
				ast.accept(this);
				return new JmStore(objs, freeObjs, env, heap);
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
							"Reference to undeclared variable " + n.varName + " as a reference to object " + n.val, n);
				}
				Type type = var.getType();
				if (type instanceof RefType) {
					RefType refType = (RefType) type;
					updateType(n.val, refType, n);
				} else {
					throw new SemanticError("Non-reference variable " + var.name
							+ " is used as a reference variable for object " + n.val, n);
				}
			}

			public void visit(ASTRefFieldVal n) {
				RefType objRefType = objNameToRefType.get(n.objName);
				if (objRefType != null) {
					Optional<Field> maybeField = objRefType.findField(n.fieldName);
					if (!maybeField.isPresent()) {
						throw new SemanticError(
								"Field " + n.fieldName + " is not defined in type " + objRefType.getName(), n);
					}
					Field field = maybeField.get();
					if (field instanceof RefField) {
						RefField refField = (RefField) field;
						updateType(n.val, refField.getDstType(), n);

					} else {
						throw new SemanticError("Non-reference field " + n.fieldName + " is used as a reference field",
								n);
					}
				}
			}

			private void updateType(String objName, RefType type, AST context) {
				if (objName == AST.NULL_VAL_NAME) {
					return;
				}
				RefType oldType = objNameToRefType.put(objName, type);
				if (oldType == null) {
					change = true;
				} else if (oldType != type) {
					throw new SemanticError("Attempt to re-define the type of object " + objName, context);
				}
			}
		}
	}
}