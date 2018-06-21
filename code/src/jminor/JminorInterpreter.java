package jminor;

import java.util.Optional;

import bgu.cs.util.treeGrammar.Node;
import bgu.cs.util.treeGrammar.Nonterminal;
import jminor.JmStore.JmErrorStore;
import pexyn.ArrayListTrace;
import pexyn.Trace;

/**
 * Interprets a program for a given store. The interpreter attempts to run even
 * on abstract programs (programs containing nonterminals) and may return the
 * top store if it cannot evaluate assignments or expressions.
 * 
 * TODO: handle stores with garbage as erroneous by returning an error store.
 * 
 * @author romanm
 */
public class JminorInterpreter extends JminorVisitor {
	public static final JminorInterpreter v = new JminorInterpreter();

	protected JmStore store;
	protected boolean resultCond;
	protected Val resultVal;
	protected Field resulField;
	protected RefType type;
	protected Trace<JmStore, Stmt> trace;
	protected int stepCounter;
	protected int maxSteps;

	/**
	 * Guesses the number of steps needed to evaluate the given statement on the
	 * given store.
	 * 
	 * TODO: compute by taking the loop-depth into account.
	 */
	public int guessMaxSteps(Stmt stmt, JmStore store) {
		return store.objects.size() * store.objects.size() + 1000;
	}

	/**
	 * Generates a trace by running the given statement on the given input store. If
	 * the run terminates without exceeding the maximal number of steps, a return
	 * statement is added.
	 */
	public Optional<Trace<JmStore, Stmt>> genTrace(Stmt n, JmStore input, int maxSteps) {
		assert n.concrete();
		this.trace = new ArrayListTrace<JmStore, Stmt>(input);
		run(n, input, maxSteps);
		if (stepCounter <= maxSteps) {
			updateTrace(store, store, RetStmt.v);
			var result = this.trace;
			this.trace = null;
			return Optional.of(result);
		} else {
			this.trace = null;
			return Optional.empty();
		}
	}

	public Optional<JmStore> run(Stmt n, JmStore input, int maxSteps) {
		assert n.concrete();
		this.stepCounter = 0;
		this.maxSteps = maxSteps;
		reset();
		store = input;
		n.accept(this);
		return Optional.of(store);
	}

	public Boolean test(BoolExpr n, JmStore input) {
		assert n.concrete();
		reset();
		store = input;
		n.accept(this);
		if (store instanceof JmErrorStore) {
			return null;
		} else {
			return resultCond ? Boolean.TRUE : Boolean.FALSE;
		}
	}

	protected void reset() {
		this.resultCond = false;
		this.store = null;
		this.resultVal = null;
	}

	protected void updateTrace(JmStore pre, JmStore post, Stmt label) {
		++stepCounter;
		if (stepCounter > maxSteps) {
			store = JmStore.error("Exceeded maximal number of steps: " + maxSteps);
		}
		if (trace != null) {
			trace.append(label, post);
		}
	}

	@Override
	public void visit(Nonterminal n) {
		if (store instanceof JmErrorStore)
			return;
		store = null;
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
		JmStore pre = store;
		n.getLhs().accept(this);
		if (store instanceof JmErrorStore) {
			store = pre;
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
		JmStore pre = store;
		n.getLhs().accept(this);
		if (store instanceof JmErrorStore) {
			store = pre;
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
		JmStore pre = store;
		n.getSub().accept(this);
		if (store instanceof JmErrorStore) {
			store = pre;
			resultCond = false;
		} else {
			resultCond = !resultCond;
		}
	}

	@Override
	public void visit(AssignStmt n) {
		JmStore pre = store;
		computeAssignStmt(n);
		updateTrace(pre, store, n);
		if (store instanceof JmErrorStore) {
			return;
		}
		if (store.containsGarbage()) {
			store = JmStore.error("memory leak!");
		}
	}

	@Override
	public void visit(ParallelAssign n) {
		JmStore pre = store;
		var rexprs = (ExprList) n.rvals();
		var rvals = new Val[rexprs.size()];
		for (int i = 0; i < rvals.length; ++i) {
			var rexpr = rexprs.get(i);
			rexpr.accept(this);
			if (store instanceof JmErrorStore) {
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
				store.assign(lvar, rvals[i]);
			} else {
				assert lexpr instanceof DerefExpr;
				var lhsDeref = (DerefExpr) lexpr;
				lhsDeref.getLhs().accept(this);
				Obj lobj = (Obj) resultVal;
				if (store instanceof JmErrorStore) {
					return;
				}
				if (lobj == Obj.NULL) {
					store = JmStore.error("illegal dereference by " + lhsDeref);
					return;
				}
				store = store.assign(lobj, lhsDeref.getField(), rvals[i]);

			}
		}

		updateTrace(pre, store, n);
		if (store instanceof JmErrorStore) {
			return;
		}
		if (store.containsGarbage()) {
			store = JmStore.error("memory leak!");
		}
	}

	private void computeAssignStmt(AssignStmt n) {
		n.getRhs().accept(this);
		if (store instanceof JmErrorStore) {
			return;
		}
		Val rval = resultVal;

		Node lhs = n.getLhs();
		if (lhs instanceof VarExpr) {
			VarExpr lhsVar = (VarExpr) lhs;
			store = store.assign(lhsVar.getVar(), rval);
		} else {
			assert lhs instanceof DerefExpr;
			DerefExpr lhsDeref = (DerefExpr) lhs;
			lhsDeref.getLhs().accept(this);
			Obj lobj = (Obj) resultVal;
			if (store instanceof JmErrorStore) {
				return;
			}
			if (lobj == Obj.NULL) {
				store = JmStore.error("illegal dereference of " + n.getLhs());
				return;
			}
			store = store.assign(lobj, lhsDeref.getField(), rval);
		}
	}

	@Override
	public void visit(DerefExpr n) {
		n.getLhs().accept(this);
		if (store instanceof JmErrorStore)
			return;
		if (resultVal == Obj.NULL) {
			store = JmStore.error("null dereference of " + n.getLhs());
		} else {
			Obj lobj = (Obj) resultVal;
			resultVal = store.eval(lobj, n.getField());
			if (resultVal == null) {
				store = JmStore.error("dereference of " + n.getLhs() + " is undefined!");
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
		JmStore pre = store;
		n.getLhs().accept(this);
		if (store instanceof JmErrorStore) {
			resultCond = false;
			store = pre;
			return;
		}
		Val lval = resultVal;

		n.getRhs().accept(this);
		if (store instanceof JmErrorStore) {
			resultCond = false;
			store = pre;
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
		JmStore pre = store;
		n.getLhs().accept(this);
		if (store instanceof JmErrorStore) {
			resultCond = false;
			store = pre;
			return;
		}
		IntVal lval = (IntVal) resultVal;

		n.getRhs().accept(this);
		if (store instanceof JmErrorStore) {
			resultCond = false;
			store = pre;
			return;
		}
		IntVal rval = (IntVal) resultVal;

		resultCond = lval != null && rval != null && lval.num < rval.num;
	}

	@Override
	public void visit(IfStmt n) {
		JmStore pre = store;
		n.getCond().accept(this);
		if (store instanceof JmErrorStore) {
			updateTrace(pre, store, n);
			return;
		}
		if (resultCond)
			n.getThenNode().accept(this);
		else if (n.getElseNode() != null)
			n.getElseNode().accept(this);
	}

	@Override
	public void visit(NewExpr n) {
		var allocResult = store.allocate(n.getType());
		if (allocResult.isPresent()) {
			resultVal = allocResult.get();
		} else {
			store = JmErrorStore.error("Allocation error, out of " + n.getType().getName() + " objects!");
		}
	}

	@Override
	public void visit(SeqStmt n) {
		for (Node sub : n.getArgs()) {
			sub.accept(this);
			if (store instanceof JmErrorStore) {
				return;
			}
		}
	}

	@Override
	public void visit(WhileStmt n) {
		JmStore pre = store;
		n.getCond().accept(this);
		if (store instanceof JmErrorStore) {
			updateTrace(pre, store, n);
			return;
		}
		while (resultCond) {
			n.getBody().accept(this);
			if (store instanceof JmErrorStore)
				return;
			if (pre.equals(store)) {
				store = JmErrorStore.error("Possibly non-terminating loop!");
				stepCounter = maxSteps + 1;
				return;
			}
			pre = store;
			n.getCond().accept(this);
			if (store instanceof JmErrorStore) {
				return;
			}
			if (stepCounter > maxSteps) {
				store = JmErrorStore.error("Possibly non-terminating loop!");
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
		if (store instanceof JmErrorStore)
			return;
		Val lval = resultVal;

		n.getRhs().accept(this);
		if (store instanceof JmErrorStore)
			return;
		Val rval = resultVal;

		if (!(lval instanceof IntVal) || !(rval instanceof IntVal)) {
			store = JmErrorStore.error("non-integer operands " + n);
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
					store = JmErrorStore.error("division by zero");
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
		resultVal = store.eval(n.getVar());
		if (resultVal == null)
			store = JmErrorStore.error("Accessed uninitialized variable " + n);
	}

	@Override
	public void visit(ValExpr n) {
		resultVal = n.getVal();
		if (resultVal == null)
			store = JmErrorStore.error("Accessed uninitialized variable " + n);
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
		resultVal = store.eval(n);
		if (resultVal == null)
			store = JmErrorStore.error("Accessed uninitialized variable " + n);
	}

	@Override
	public void visit(IntVar n) {
		resultVal = store.eval(n);
		if (resultVal == null)
			store = JmErrorStore.error("Accessed uninitialized variable " + n);
	}

	@Override
	public void visit(RefType n) {
		type = n;
	}
}