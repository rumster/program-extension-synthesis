package heap;

import gp.Plan;
import grammar.Node;
import grammar.Nonterminal;
import heap.Store.ErrorStore;
import heap.Store.Obj;

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

	public Store apply(Node n, Store input, Plan<Store, Node> trace) {
		this.trace = trace;
		if (trace != null && trace.isEmpty())
			trace.setFirst(input);
		return apply(n, input);
	}

	public Store apply(Node n, Store input) {
		reset();
		state = input;
		n.accept(this);
		return state;
	}

	public Boolean test(Node n, Store input) {
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
		if (n.numOfNonterminals > 0) {
			state = null;
			updateTrace(pre, state, n);
			return;
		}

		calcState(n);
		updateTrace(pre, state, n);
		if (state instanceof ErrorStore) {
			return;
		}
		if (state.containsGarbage()) {
			state = Store.error("memory leak!");
		}
	}

	/**
	 * Calculates and updates this with the given assignment.
	 */
	private void calcState(AssignStmt n) {
		Node lhs = n.getLhs();
		Node rhs = n.getRhs();
		if (lhs instanceof Var) {
			Var lhsVar = (Var) lhs;
			if (rhs instanceof Var) {
				n.getRhs().accept(this);
				if (state instanceof ErrorStore) {
					return;
				}
				Val rval = resultVal;
				state = state.assign(lhsVar, rval);
			} else if (rhs instanceof DerefExpr) {
				n.getRhs().accept(this);
				if (state instanceof ErrorStore) {
					return;
				}
				Val rval = resultVal;
				state = state.assign(lhsVar, rval);
			} else if (rhs instanceof NewExpr) {
				throw new IllegalArgumentException("New expression is non-deterministic!");
			} else if (rhs == NullExpr.v) {
				state = state.assign(lhsVar, Obj.NULL);
			} else {
				assert false : "unexpected case!";
			}
		} else if (lhs instanceof DerefExpr) {
			n.getRhs().accept(this);
			if (state instanceof ErrorStore) {
				return;
			}
			Val robj = resultVal;
			DerefExpr accessPath = (DerefExpr) lhs;
			accessPath.getLhs().accept(this);
			Obj lobj = (Obj) resultVal;
			if (state instanceof ErrorStore) {
				return;
			}
			if (resultVal == Obj.NULL) {
				state = Store.error("illegal dereference of " + n.getLhs());
				return;
			}
			state = state.assign(lobj, accessPath.getField(), robj);
		} else {
			assert false : "unexpected case!";
		}
	}

	@Override
	public void visit(DerefExpr n) {
		if (n.getLhs().numOfNonterminals > 0) {
			state = null;
			return;
		}

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
		if (n.getLhs().numOfNonterminals > 0 || n.getRhs().numOfNonterminals > 0) {
			state = null;
			return;
		}

		n.getLhs().accept(this);
		if (state instanceof ErrorStore)
			return;
		Val lval = resultVal;

		n.getRhs().accept(this);
		if (state instanceof ErrorStore)
			return;
		Val rval = resultVal;

		resultCond = lval == rval;
	}

	@Override
	public void visit(LeqExpr n) {
		if (n.getLhs().numOfNonterminals > 0 || n.getRhs().numOfNonterminals > 0) {
			state = null;
			return;
		}

		n.getLhs().accept(this);
		if (state instanceof ErrorStore)
			return;
		IntVal lval = (IntVal) resultVal;

		n.getRhs().accept(this);
		if (state instanceof ErrorStore)
			return;
		IntVal rval = (IntVal) resultVal;

		resultCond = lval.num <= rval.num;
	}

	@Override
	public void visit(LtExpr n) {
		if (n.getLhs().numOfNonterminals > 0 || n.getRhs().numOfNonterminals > 0) {
			state = null;
			return;
		}

		n.getLhs().accept(this);
		if (state instanceof ErrorStore)
			return;
		IntVal lval = (IntVal) resultVal;

		n.getRhs().accept(this);
		if (state instanceof ErrorStore)
			return;
		IntVal rval = (IntVal) resultVal;

		resultCond = lval.num < rval.num;
	}

	@Override
	public void visit(IfStmt n) {
		Store pre = state;
		if (n.getCond().numOfNonterminals > 0) {
			state = null;
			updateTrace(pre, state, n);
			return;
		}

		n.getCond().accept(this);
		if (state instanceof ErrorStore) {
			updateTrace(pre, state, n);
			return;
		}
		if (resultCond)
			n.getThen().accept(this);
		else if (n.getElseNode() != null)
			n.getElseNode().accept(this);
	}

	@Override
	public void visit(NewExpr n) {
		assert false;
	}

	@Override
	public void visit(NotExpr n) {
		if (n.getSub().numOfNonterminals > 0) {
			state = null;
			return;
		}

		n.getSub().accept(this);
		if (state instanceof ErrorStore)
			return;
		resultCond = !resultCond;
	}

	@Override
	public void visit(SequenceStmt n) {
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
		if (n.getCond().numOfNonterminals > 0) {
			state = null;
			updateTrace(pre, state, n);
			return;
		}

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
	public void visit(NullExpr n) {
		resultVal = Obj.NULL;
	}

	@Override
	public void visit(IntVal n) {
		resultVal = new IntVal(n.num);
	}

	@Override
	public void visit(MinusExpr n) {
		if (n.getSub().numOfNonterminals > 0) {
			state = null;
			return;
		}

		n.getSub().accept(this);
		if (state instanceof ErrorStore)
			return;

		if (!(resultVal instanceof IntVal)) {
			state = ErrorStore.error("non-integer operand " + n);
		} else {
			int value = ((IntVal) resultVal).num;
			resultVal = new IntVal(-value);
		}
	}

	@Override
	public void visit(PlusExpr n) {
		if (n.getLhs().numOfNonterminals > 0 || n.getRhs().numOfNonterminals > 0) {
			state = null;
			return;
		}

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
			resultVal = new IntVal(((IntVal) lval).num + ((IntVal) rval).num);
		}
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