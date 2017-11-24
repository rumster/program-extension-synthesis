package heap;

import gp.Plan;
import heap.State.ErrorState;
import heap.State.Obj;
import heap.State.TerminalState;
import heap.State.Val;
import treeGrammar.Node;
import treeGrammar.Nonterminal;
import treeGrammar.InternalNode;

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
public class Interpreter extends PWhileVisitor {
	public static final Interpreter v = new Interpreter();

	protected State state;
	protected boolean resultCond;
	protected Val resultVal;
	protected Field resulField;
	protected RefType type;
	protected Plan<State, InternalNode> trace;

	public State apply(Node n, State input, Plan<State, InternalNode> trace) {
		this.trace = trace;
		if (trace != null && trace.isEmpty())
			trace.setFirst(input);
		return apply(n, input);
	}

	public State apply(Node n, State input) {
		reset();
		state = input;
		n.accept(this);
		return state;
	}

	public Boolean test(Node n, State input) {
		reset();
		state = input;
		n.accept(this);
		if (state instanceof TerminalState) {
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

	protected void updateTrace(State pre, State post, InternalNode label) {
		if (trace != null) {
			trace.append(label, post);
		}
	}

	@Override
	public void visit(Nonterminal n) {
		if (state instanceof TerminalState)
			return;
		state = State.top;
	}

	@Override
	public void visit(OpAnd n) {
		n.getLhs().accept(this);
		if (state instanceof TerminalState)
			return;
		if (resultCond) {
			n.getRhs().accept(this);
		}
	}

	@Override
	public void visit(OpOr n) {
		n.getLhs().accept(this);
		if (state instanceof TerminalState)
			return;
		if (!resultCond) {
			n.getRhs().accept(this);
		}
	}

	@Override
	public void visit(OpAssgn n) {
		State pre = state;
		if (n.numOfNonterminals > 0) {
			state = State.top;
			updateTrace(pre, state, n);
			return;
		}

		calcState(n);
		updateTrace(pre, state, n);
		if (state instanceof TerminalState) {
			return;
		}
		if (state.containsGarbage()) {
			state = State.errorState("memory leak!");
		}
	}

	/**
	 * @brief Calculates and updates this.state for after running n
	 * @param n
	 *            - Grammar node to act upon.
	 */
	private void calcState(OpAssgn n) {
		Node lhs = n.getLhs();
		Node rhs = n.getRhs();
		if (lhs instanceof Var) {
			Var lhsVar = (Var) lhs;
			if (rhs instanceof Var) {
				n.getRhs().accept(this);
				if (state instanceof TerminalState) {
					return;
				}
				Val rval = resultVal;
				state = state.assign(lhsVar, rval);
			} else if (rhs instanceof OpDeref) {
				n.getRhs().accept(this);
				if (state instanceof TerminalState) {
					return;
				}
				Val rval = resultVal;
				state = state.assign(lhsVar, rval);
			} else if (rhs instanceof OpNew) {
				RefVar lhsRefVar = (RefVar) lhs;
				state = state.assignNewObj(lhsRefVar);
			} else if (rhs == Null.v) {
				state = state.assign(lhsVar, Obj.NULL);
			} else {
				assert false : "unexpected case!";
			}
		} else if (lhs instanceof OpDeref) {
			n.getRhs().accept(this);
			if (state instanceof TerminalState) {
				return;
			}
			Val robj = resultVal;
			OpDeref accessPath = (OpDeref) lhs;
			accessPath.getLhs().accept(this);
			Obj lobj = (Obj) resultVal;
			if (state instanceof TerminalState) {
				return;
			}
			if (resultVal == Obj.NULL) {
				state = State.errorState("illegal dereference of " + n.getLhs());
				return;
			}
			state = state.assign(lobj, accessPath.getField(), robj);
		} else {
			assert false : "unexpected case!";
		}
	}

	@Override
	public void visit(OpDeref n) {
		if (n.getLhs().numOfNonterminals > 0) {
			state = State.top;
			return;
		}

		n.getLhs().accept(this);
		if (state instanceof TerminalState)
			return;
		if (resultVal == Obj.NULL) {
			state = State.errorState("illegal dereference of " + n.getLhs());
		} else {
			Obj lobj = (Obj) resultVal;
			resultVal = state.eval(lobj, n.getField());
		}
	}

	@Override
	public void visit(OpEq n) {
		if (n.getLhs().numOfNonterminals > 0 || n.getRhs().numOfNonterminals > 0) {
			state = State.top;
			return;
		}

		n.getLhs().accept(this);
		if (state instanceof TerminalState)
			return;
		Val lval = resultVal;

		n.getRhs().accept(this);
		if (state instanceof TerminalState)
			return;
		Val rval = resultVal;

		resultCond = lval == rval;
	}

	@Override
	public void visit(OpLeq n) {
		if (n.getLhs().numOfNonterminals > 0 || n.getRhs().numOfNonterminals > 0) {
			state = State.top;
			return;
		}

		n.getLhs().accept(this);
		if (state instanceof TerminalState)
			return;
		State.Int lval = (State.Int) resultVal;

		n.getRhs().accept(this);
		if (state instanceof TerminalState)
			return;
		State.Int rval = (State.Int) resultVal;

		resultCond = lval.num <= rval.num;
	}

	@Override
	public void visit(OpLt n) {
		if (n.getLhs().numOfNonterminals > 0 || n.getRhs().numOfNonterminals > 0) {
			state = State.top;
			return;
		}

		n.getLhs().accept(this);
		if (state instanceof TerminalState)
			return;
		State.Int lval = (State.Int) resultVal;

		n.getRhs().accept(this);
		if (state instanceof TerminalState)
			return;
		State.Int rval = (State.Int) resultVal;

		resultCond = lval.num < rval.num;
	}

	@Override
	public void visit(OpIf n) {
		State pre = state;
		if (n.getCond().numOfNonterminals > 0) {
			state = State.top;
			updateTrace(pre, state, n);
			return;
		}

		n.getCond().accept(this);
		if (state instanceof TerminalState) {
			updateTrace(pre, state, n);
			return;
		}
		if (resultCond)
			n.getThen().accept(this);
		else if (n.getElseNode() != null)
			n.getElseNode().accept(this);
	}

	@Override
	public void visit(OpNew n) {
		assert false;
	}

	@Override
	public void visit(OpNot n) {
		if (n.getSub().numOfNonterminals > 0) {
			state = State.top;
			return;
		}

		n.getSub().accept(this);
		if (state instanceof TerminalState)
			return;
		resultCond = !resultCond;
	}

	@Override
	public void visit(OpSeq n) {
		for (Node sub : n.getArgs()) {
			sub.accept(this);
			if (state instanceof TerminalState) {
				return;
			}
		}
	}

	@Override
	public void visit(OpWhile n) {
		State pre = state;
		if (n.getCond().numOfNonterminals > 0) {
			state = State.top;
			updateTrace(pre, state, n);
			return;
		}

		n.getCond().accept(this);
		if (state instanceof TerminalState) {
			updateTrace(pre, state, n);
			return;
		}
		int iterationBound = state.getObjects().size() * 2;
		int iterationCounter = 0;
		while (resultCond) {
			n.getBody().accept(this);
			if (state instanceof TerminalState)
				return;
			pre = state;
			n.getCond().accept(this);
			if (state instanceof TerminalState) {
				return;
			}
			++iterationCounter;
			if (iterationCounter > iterationBound) {
				state = ErrorState.errorState("Possibly non-terminating loop!");
				return;
			}
		}
	}

	@Override
	public void visit(OpSkip n) {
	}

	@Override
	public void visit(Null n) {
		resultVal = Obj.NULL;
	}

	@Override
	public void visit(Int n) {
		resultVal = new State.Int(n.value);
	}

	@Override
	public void visit(OpMinus n) {
		if (n.getSub().numOfNonterminals > 0) {
			state = State.top;
			return;
		}

		n.getSub().accept(this);
		if (state instanceof TerminalState)
			return;

		if (!(resultVal instanceof State.Int)) {
			state = ErrorState.errorState("non-integer operand " + n);
		} else {
			int value = ((State.Int) resultVal).num;
			resultVal = new State.Int(-value);
		}
	}

	@Override
	public void visit(OpPlus n) {
		if (n.getLhs().numOfNonterminals > 0 || n.getRhs().numOfNonterminals > 0) {
			state = State.top;
			return;
		}

		n.getLhs().accept(this);
		if (state instanceof TerminalState)
			return;
		Val lval = resultVal;

		n.getRhs().accept(this);
		if (state instanceof TerminalState)
			return;
		Val rval = resultVal;

		if (!(lval instanceof State.Int) || !(rval instanceof State.Int)) {
			state = ErrorState.errorState("non-integer operands " + n);
		} else {
			resultVal = new State.Int(((State.Int) lval).num + ((State.Int) rval).num);
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
			state = ErrorState.errorState("Accessed uninitialized variable " + n);
	}

	@Override
	public void visit(IntVar n) {
		resultVal = state.eval(n);
		if (resultVal == null)
			state = ErrorState.errorState("Accessed uninitialized variable " + n);
	}

	@Override
	public void visit(RefType n) {
		type = n;
	}
}