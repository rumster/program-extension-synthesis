package jminor;

import java.util.Optional;

import grammar.Node;
import grammar.Nonterminal;
import jminor.Store.ErrorStore;
import pexyn.ArrayListPlan;
import pexyn.Plan;

/**
 * Interprets a program for a given store. The interpreter attempts to run even
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
	protected Plan<Store, Stmt> trace;
	protected int stepCounter;
	protected int maxSteps;

	/**
	 * Guesses the number of steps needed to evaluate the given statement on the
	 * given store.
	 * 
	 * TODO: compute by taking the loop-depth into account.
	 */
	public int guessMaxSteps(Stmt stmt, Store store) {
		return store.objects.size() * store.objects.size() + 1000;
	}

	/**
	 * Generates a trace by running the given statement on the given input store. If
	 * the run terminates without exceeding the maximal number of steps, a return
	 * statement is added.
	 */
	public Optional<Plan<Store, Stmt>> genTrace(Stmt n, Store input, int maxSteps) {
		assert n.concrete();
		this.trace = new ArrayListPlan<Store, Stmt>(input);
		run(n, input, maxSteps);
		if (stepCounter <= maxSteps) {
			updateTrace(state, state, RetStmt.v);
			var result = this.trace;
			this.trace = null;
			return Optional.of(result);
		} else {
			this.trace = null;
			return Optional.empty();
		}
	}

	public Optional<Store> run(Stmt n, Store input, int maxSteps) {
		assert n.concrete();
		this.stepCounter = 0;
		this.maxSteps = maxSteps;
		reset();
		state = input;
		n.accept(this);
		return Optional.of(state);
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

	protected void updateTrace(Store pre, Store post, Stmt label) {
		++stepCounter;
		if (stepCounter > maxSteps) {
			state = Store.error("Exceeded maximal number of steps: " + maxSteps);
		}
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

	/**
	 * If an evaluation of the expression causes an error, the whole expression
	 * evaluates to false. The store is not affected (effectively, rolled back to
	 * the input store).
	 */
	@Override
	public void visit(AndExpr n) {
		Store pre = state;
		n.getLhs().accept(this);
		if (state instanceof ErrorStore) {
			state = pre;
			resultCond = false;
		}
		if (resultCond) {
			n.getRhs().accept(this);
		}
	}

	/**
	 * If an evaluation of the expression causes an error, the whole expression
	 * evaluates to false. The store is not affected (effectively, rolled back to
	 * the input store).
	 */
	@Override
	public void visit(OrExpr n) {
		Store pre = state;
		n.getLhs().accept(this);
		if (state instanceof ErrorStore) {
			state = pre;
			resultCond = false;
		}
		if (!resultCond) {
			n.getRhs().accept(this);
		}
	}

	/**
	 * If an evaluation of the expression causes an error, the whole expression
	 * evaluates to false. The store is not affected (effectively, rolled back to
	 * the input store).
	 */
	@Override
	public void visit(NotExpr n) {
		Store pre = state;
		n.getSub().accept(this);
		if (state instanceof ErrorStore) {
			state = pre;
			resultCond = false;
		} else {
			resultCond = !resultCond;
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

	@Override
	public void visit(ParallelAssign n) {
		Store pre = state;
		var rexprs = (ExprList) n.rvals();
		var rvals = new Val[rexprs.size()];
		for (int i = 0; i < rvals.length; ++i) {
			var rexpr = rexprs.get(i);
			rexpr.accept(this);
			if (state instanceof ErrorStore) {
				return;
			}
			rvals[i] = resultVal;
		}

		var lexprs = (ExprList) n.rvals();
		for (int i = 0; i < lexprs.size(); ++i) {
			var lexpr = lexprs.get(i);
			if (lexpr instanceof VarExpr) {
				var lvarExpr = (VarExpr) lexpr;
				var lvar = lvarExpr.getVar();
				state.assign(lvar, rvals[i]);
			} else {
				assert lexpr instanceof DerefExpr;
				var lhsDeref = (DerefExpr) lexpr;
				lhsDeref.getLhs().accept(this);
				Obj lobj = (Obj) resultVal;
				if (state instanceof ErrorStore) {
					return;
				}
				if (lobj == Obj.NULL) {
					state = Store.error("illegal dereference by " + lhsDeref);
					return;
				}
				state = state.assign(lobj, lhsDeref.getField(), rvals[i]);

			}
		}

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
			state = Store.error("null dereference of " + n.getLhs());
		} else {
			Obj lobj = (Obj) resultVal;
			resultVal = state.eval(lobj, n.getField());
			if (resultVal == null) {
				state = Store.error("dereference of " + n.getLhs() + " is undefined!");
			}
		}
	}

	/**
	 * If an evaluation of the expression causes an error, the whole expression
	 * evaluates to false. The store is not affected (effectively, rolled back to
	 * the input store).
	 */
	@Override
	public void visit(EqExpr n) {
		Store pre = state;
		n.getLhs().accept(this);
		if (state instanceof ErrorStore) {
			resultCond = false;
			state = pre;
			return;
		}
		Val lval = resultVal;

		n.getRhs().accept(this);
		if (state instanceof ErrorStore) {
			resultCond = false;
			state = pre;
			return;
		}
		Val rval = resultVal;

		resultCond = lval != null && rval != null && lval.equals(rval);
	}

	/**
	 * If an evaluation of the expression causes an error, the whole expression
	 * evaluates to false. The store is not affected (effectively, rolled back to
	 * the input store).
	 */
	@Override
	public void visit(LtExpr n) {
		Store pre = state;
		n.getLhs().accept(this);
		if (state instanceof ErrorStore) {
			resultCond = false;
			state = pre;
			return;
		}
		IntVal lval = (IntVal) resultVal;

		n.getRhs().accept(this);
		if (state instanceof ErrorStore) {
			resultCond = false;
			state = pre;
			return;
		}
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
		while (resultCond) {
			n.getBody().accept(this);
			if (state instanceof ErrorStore)
				return;
			if (pre.equals(state)) {
				state = ErrorStore.error("Possibly non-terminating loop!");
				stepCounter = maxSteps + 1;
				return;
			}
			pre = state;
			n.getCond().accept(this);
			if (state instanceof ErrorStore) {
				return;
			}
			if (stepCounter > maxSteps) {
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
			var lhsNum = ((IntVal) lval).num;
			var rhsNum = ((IntVal) rval).num;
			switch (n.op) {
			case PLUS:
				resultVal = new IntVal(lhsNum + rhsNum);
				break;
			case MINUS:
				resultVal = new IntVal(lhsNum - rhsNum);
				break;
			case TIMES:
				resultVal = new IntVal(lhsNum * rhsNum);
				break;
			case DIVIDE:
				if (rhsNum == 0) {
					state = ErrorStore.error("division by zero");
				} else {
					resultVal = new IntVal(lhsNum / rhsNum);
				}
				break;
			case LT:
				resultCond = lhsNum < rhsNum;
				break;
			case LEQ:
				resultCond = lhsNum <= rhsNum;
				break;
			case GT:
				resultCond = lhsNum > rhsNum;
				break;
			case GEQ:
				resultCond = lhsNum >= rhsNum;
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