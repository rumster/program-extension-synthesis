package heap;

import gp.Plan;
import grammar.Node;
import grammar.Nonterminal;
import heap.Store.ErrorStore;

/**
 * Interprets a program for a given state. The interpreter attempts to run even
 * on abstract programs (programs containing nonterminals) and may return the
 * top state if it cannot evaluate assignments or expressions.
 * 
 * TODO: handle non-terminating loops. Currently, if the number of iterations is
 * more than the number of heap objects, we can consider the loop to be
 * non-terminating.
 * 
 * TODO: handle states with garbage as erroneous by returning an error state.
 * 
 * @author romanm
 */
public class PWhileInterpreter extends PWhileVisitor {
	public static final PWhileInterpreter v = new PWhileInterpreter();

	protected Store state;
	protected boolean resultCond;
	protected Val resultVal;
	protected Field resulField;
	protected RefType type;
	protected Plan<Store, Node> trace;

	public Store apply(Stmt n, Store input, Plan<Store, Node> trace) {
		assert n.concrete();
		this.trace = trace;
		if (trace != null && trace.isEmpty())
			trace.setFirst(input);
		return apply(n, input);
	}

	public Store apply(Stmt n, Store input) {
		assert n.concrete();
		reset();
		state = input;
		n.accept(this);
		return state;
	}

	public Boolean test(BoolExpr n, Store input) {
		assert n.concrete();
		reset();
		state = input;
		n.accept(this);
		if (state instanceof ErrorStore) {
			return null;
		} else {
			return resultCond ? Boolean.TRUE : Boolean.FALSE;
		}
	}

	protected void reset() {
		this.resultCond = false;
		this.state = null;
		this.resultVal = null;
	}

	protected void updateTrace(Store pre, Store post, Node label) {
		if (trace != null) {
			trace.append(label, post);
		}
	}

	@Override
	public void visit(Nonterminal n) {
		if (state instanceof ErrorStore)
			return;
		state = null;
	}

	@Override
	public void visit(True n) {
		resultCond = true;
	}

	@Override
	public void visit(AndExpr n) {
		n.getLhs().accept(this);
		if (state instanceof ErrorStore)
			return;
		if (resultCond) {
			n.getRhs().accept(this);
		}
	}

	@Override
	public void visit(OrExpr n) {
		n.getLhs().accept(this);
		if (state instanceof ErrorStore)
			return;
		if (!resultCond) {
			n.getRhs().accept(this);
		}
	}

	@Override
	public void visit(AssignStmt n) {
		Store pre = state;
		computeAssignStmt(n);
		updateTrace(pre, state, n);
		if (state instanceof ErrorStore) {
			return;
		}
		if (state.containsGarbage()) {
			state = Store.error("memory leak!");
		}
	}

	private void computeAssignStmt(AssignStmt n) {
		n.getRhs().accept(this);
		if (state instanceof ErrorStore) {
			return;
		}
		Val rval = resultVal;

		Node lhs = n.getLhs();
		if (lhs instanceof VarExpr) {
			VarExpr lhsVar = (VarExpr) lhs;
			state = state.assign(lhsVar.getVar(), rval);
		} else {
			assert lhs instanceof DerefExpr;
			DerefExpr lhsDeref = (DerefExpr) lhs;
			lhsDeref.getLhs().accept(this);
			Obj lobj = (Obj) resultVal;
			if (state instanceof ErrorStore) {
				return;
			}
			if (lobj == Obj.NULL) {
				state = Store.error("illegal dereference of " + n.getLhs());
				return;
			}
			state = state.assign(lobj, lhsDeref.getField(), rval);
		}
	}

	@Override
	public void visit(DerefExpr n) {
		n.getLhs().accept(this);
		if (state instanceof ErrorStore)
			return;
		if (resultVal == Obj.NULL) {
			state = Store.error("illegal dereference of " + n.getLhs());
		} else {
			Obj lobj = (Obj) resultVal;
			resultVal = state.eval(lobj, n.getField());
		}
	}

	@Override
	public void visit(EqExpr n) {
		n.getLhs().accept(this);
		if (state instanceof ErrorStore)
			return;
		Val lval = resultVal;

		n.getRhs().accept(this);
		if (state instanceof ErrorStore)
			return;
		Val rval = resultVal;

		resultCond = lval != null && rval != null && lval == rval;
	}

	@Override
	public void visit(LtExpr n) {
		n.getLhs().accept(this);
		if (state instanceof ErrorStore)
			return;
		IntVal lval = (IntVal) resultVal;

		n.getRhs().accept(this);
		if (state instanceof ErrorStore)
			return;
		IntVal rval = (IntVal) resultVal;

		resultCond = lval != null && rval != null && lval.num < rval.num;
	}

	@Override
	public void visit(IfStmt n) {
		Store pre = state;
		n.getCond().accept(this);
		if (state instanceof ErrorStore) {
			updateTrace(pre, state, n);
			return;
		}
		if (resultCond)
			n.getThenNode().accept(this);
		else if (n.getElseNode() != null)
			n.getElseNode().accept(this);
	}

	@Override
	public void visit(NewExpr n) {
		var allocResult = state.allocate(n.getType());
		if (allocResult.isPresent()) {
			resultVal = allocResult.get();
		} else {
			state = ErrorStore.error("Allocation error, out of " + n.getType().getName() + " objects!");
		}
	}

	@Override
	public void visit(NotExpr n) {
		n.getSub().accept(this);
		if (state instanceof ErrorStore)
			return;
		resultCond = !resultCond;
	}

	@Override
	public void visit(SeqStmt n) {
		for (Node sub : n.getArgs()) {
			sub.accept(this);
			if (state instanceof ErrorStore) {
				return;
			}
		}
	}

	@Override
	public void visit(WhileStmt n) {
		Store pre = state;
		n.getCond().accept(this);
		if (state instanceof ErrorStore) {
			updateTrace(pre, state, n);
			return;
		}
		int iterationBound = state.getObjects().size() * 2;
		int iterationCounter = 0;
		while (resultCond) {
			n.getBody().accept(this);
			if (state instanceof ErrorStore)
				return;
			pre = state;
			n.getCond().accept(this);
			if (state instanceof ErrorStore) {
				return;
			}
			++iterationCounter;
			if (iterationCounter > iterationBound) {
				state = ErrorStore.error("Possibly non-terminating loop!");
				return;
			}
		}
	}

	@Override
	public void visit(SkipStmt n) {
	}

	@Override
	public void visit(RetStmt n) {
		// TODO: drop local variables and check for garbage.
	}

	@Override
	public void visit(NullExpr n) {
		resultVal = Obj.NULL;
	}

	@Override
	public void visit(IntVal n) {
		resultVal = new IntVal(n.num);
	}

	@Override
	public void visit(IntBinOpExpr n) {
		n.getLhs().accept(this);
		if (state instanceof ErrorStore)
			return;
		Val lval = resultVal;

		n.getRhs().accept(this);
		if (state instanceof ErrorStore)
			return;
		Val rval = resultVal;

		if (!(lval instanceof IntVal) || !(rval instanceof IntVal)) {
			state = ErrorStore.error("non-integer operands " + n);
		} else {
			var lhsVal = ((IntVal) lval).num;
			var rhsVal = ((IntVal) rval).num;
			switch (n.op) {
			case PLUS:
				resultVal = new IntVal(lhsVal + rhsVal);
				break;
			case MINUS:
				resultVal = new IntVal(lhsVal - rhsVal);
				break;
			case TIMES:
				resultVal = new IntVal(lhsVal * rhsVal);
				break;
			case DIVIDE:
				if (rhsVal == 0) {
					state = ErrorStore.error("division by zero");
				} else {
					resultVal = new IntVal(lhsVal + rhsVal);
				}
				break;
			default:
				throw new IllegalArgumentException("Encountered unsupported operator: " + n.op);
			}
		}
	}

	@Override
	public void visit(VarExpr n) {
		resultVal = state.eval(n.getVar());
		if (resultVal == null)
			state = ErrorStore.error("Accessed uninitialized variable " + n);
	}

	@Override
	public void visit(ValExpr n) {
		resultVal = n.getVal();
		if (resultVal == null)
			state = ErrorStore.error("Accessed uninitialized variable " + n);
	}

	@Override
	public void visit(RefField n) {
		resulField = n;
	}

	@Override
	public void visit(IntField n) {
		resulField = n;
	}

	@Override
	public void visit(RefVar n) {
		resultVal = state.eval(n);
		if (resultVal == null)
			state = ErrorStore.error("Accessed uninitialized variable " + n);
	}

	@Override
	public void visit(IntVar n) {
		resultVal = state.eval(n);
		if (resultVal == null)
			state = ErrorStore.error("Accessed uninitialized variable " + n);
	}

	@Override
	public void visit(RefType n) {
		type = n;
	}
}