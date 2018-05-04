package heap;

/**
 * Used to Generate synthesis problems for singly-linked lists, now just
 * generates perfect traces for them.
 * 
 * @author romanm
 */
public class SLLTraceGen {
//	private static Random random = new Random();
//
//	public static final RefType sllType = new RefType("SLL");
//	public static final RefField next = new RefField("n", sllType, sllType, false);
//	public static final IntField val = new IntField("d", sllType, false);
//
//	public static final RefVar head = new RefVar("head", sllType, VarRole.ARG, false, false);
//	public static final RefVar first = new RefVar("first", sllType, VarRole.ARG, false, false);
//	public static final RefVar second = new RefVar("second", sllType, VarRole.ARG, false, false);
//	// output variable: out=true
//	public static final RefVar ret = new RefVar("ret", sllType, VarRole.ARG, true, false);
//	public static final RefVar t1 = new RefVar("t1", sllType);
//	public static final RefVar t2 = new RefVar("t2", sllType);
//	// public static final RefVar v1 = new RefVar("v1", Int.type);
//
//	private static final PWhileGrammarGen ggen = new PWhileGrammarGen();
//	@SuppressWarnings("unused") // it's needed for internal static
//								// initialization
//	private static final Grammar grammar = ggen.gen(Arrays.asList(head, ret, t1, t2), Arrays.asList(sllType, IntVal.type));
//
//	public static Store genSortedSLL(Store input, RefVar head, int length) {
//		return genSLL(input, head, length, true, false);
//	}
//
//	public static Store genUnsortedSLL(Store input, RefVar head, int length) {
//		return genSLL(input, head, length, false, false);
//	}
//
//	// TODO add cyclic sll benchmarks
//	// private static Store genCyclicSLL(Store input, RefVar head, int length)
//	// {
//	// return genSLL(input, head, length, false, true);
//	// }
//
//	/*
//	 * @sorted: true - generate sorted list in descending order (to be consistent
//	 * with Benchmarks)
//	 */
//	private static Store genSLL(Store input, RefVar head, int length, boolean sorted, boolean cyclic) {
//		assert (!(sorted && cyclic));
//		input = input.assignNewObj(head);
//		Obj tail = input.eval(head);
//		input = input.assign(t1, tail);
//		input = input.assign(tail, val, new Int(10 * length + random.nextInt(20)));
//		for (int i = 0; i < length; ++i) {
//			input = input.assignNewObj(t2);
//			Obj newTail = input.eval(t2);
//			input = input.assign(tail, next, newTail);
//			Int value = sorted ? ((Int) input.getFields(tail).get(val)).add(new Int(-random.nextInt(10 + i) - i))
//					: new Int(random.nextInt());
//
//			input = input.assign(newTail, val, value);
//			input = input.assign(t1, newTail);
//			tail = newTail;
//		}
//		if (cyclic)
//			input.assign(t1, head);
//		ArrayList<TVar> dead = new ArrayList<>();
//		dead.add(t1);
//		dead.add(t2);
//		input = input.clean(dead);
//		return input;
//	}
//
//	// -------------------------------------------------------------------------------------------------------------------------------------
//
//	/**
//	 * Generates the following program: <code>
//	 * y := null;
//	 * while (x != null) {
//	 *   t := x.n;
//	 *   x.n := y;
//	 *   y := x;
//	 *   x := t;
//	 * }
//	 * </code>
//	 */
//	public static Operator genReverseProgram() {
//		ArrayList<Node> assgns = new ArrayList<>();
//		assgns.add(new OpAssgn(t1, new OpDeref(head, next)));
//		assgns.add(new OpAssgn(new OpDeref(head, next), ret));
//		assgns.add(new OpAssgn(ret, head));
//		assgns.add(new OpAssgn(head, t1));
//		Operator prog = new OpSeq(new OpAssgn(ret, TNull.v),
//				new OpWhile(new OpNot(new OpEq(head, TNull.v)), new OpSeq(assgns)));
//		return prog;
//	}
//
//	private static Store genReverseInput(int length) {
//		Store input = genUnsortedSLL(new Store(), head, length);
//		input = input.assign(ret, Obj.NULL);
//		input = input.assign(t1, Obj.NULL);
//		return input;
//	}
//
//	public static Trace genReverse(int length) {
//		Store input = genReverseInput(length);
//		return genReverse(input);
//	}
//
//	public static Trace genReverse(Store input) {
//		Operator prog = genReverseProgram();
//		Trace trace = new Trace();
//		Store output = Interpreter.v.apply(prog, input, trace);
//		assert (!(output instanceof ErrorStore));
//		return trace;
//	}
//
//	public static boolean VerifyReverse(ProgramGrammar g, int length) {
//		Operator prog = genReverseProgram();
//		Store input = genReverseInput(length);
//		Store pOutput = Interpreter.v.apply(prog, input);
//		Store gOutput = GrammarInterpreter.v.apply(g, input);
//		return gOutput.equals(pOutput);
//	}
//	// -------------------------------------------------------------------------------------------------------------------------------------
//
//	// -------------------------------------------------------------------------------------------------------------------------------------
//
//	/**
//	 * Generates an Operator for merging between two lists sorted in descending
//	 * order (to be consistent with Benchmarks)
//	 */
//	public static Operator genMergeListsProgram() {
//		ArrayList<Operator> switchSeq = new ArrayList<>();
//		switchSeq.add(new OpAssgn(ret, second));
//		switchSeq.add(new OpAssgn(second, first));
//		switchSeq.add(new OpAssgn(first, ret));
//
//		ArrayList<Operator> seq = new ArrayList<>();
//		// making sure ret points to the head with the highest value
//		seq.add(new OpIf(new OpLt(new OpDeref(second, val), new OpDeref(first, val)), new OpAssgn(ret, first),
//				new OpSeq(switchSeq)));
//
//		ArrayList<Operator> loop = new ArrayList<>();
//		loop.add(new OpAssgn(t1, new OpDeref(first, next)));
//		loop.add(new OpAssgn(new OpDeref(first, next), second));
//		loop.add(new OpAssgn(second, t1));
//
//		seq.add(new OpWhile(new OpNot(new OpEq(new OpDeref(first, next), TNull.v)),
//				new OpSeq(new OpIf(new OpLt(new OpDeref(new OpDeref(first, next), val), new OpDeref(second, val)),
//						new OpSeq(loop), null), new OpAssgn(first, new OpDeref(first, next)))));
//
//		seq.add(new OpIf(new OpEq(new OpDeref(first, next), TNull.v), new OpAssgn(new OpDeref(first, next), second),
//				null));
//		Operator prog = new OpSeq(seq);
//		return prog;
//	}
//	
//	/*
//	 * Generates an Operator for merging between two lists sorted in descending
//	 * order (to be consistent with Benchmarks):
//	 	  
//		ret = NULL;
//		while (first != null || second != null) {
//		    if (second == null || first != null && first.d >= second.d) {
//		        t1 = first;
//		        first = first.n;
//		        t1.n = ret;
//		        ret = t1;
//		        t1 = NULL;
//		    }
//		    else {
//		        t1 = second;
//		        second = second.n;
//		        t1.n = ret;
//		        ret = t1;
//		        t1 = NULL;
//		    }
//		}
//	 */
//	public static Operator genMergeListsProgram2() {		
//		ArrayList<Operator> saveFirstSeq = new ArrayList<>();
//		saveFirstSeq.add(new OpAssgn(t1, first));
//		saveFirstSeq.add(new OpAssgn(first, new OpDeref(first, next)));
//		saveFirstSeq.add(new OpAssgn(new OpDeref(t1, next), ret));
//		saveFirstSeq.add(new OpAssgn(ret, t1));	
//		saveFirstSeq.add(new OpAssgn(t1, TNull.v));	
//		
//		
//		ArrayList<Operator> saveSecondSeq = new ArrayList<>();
//		saveSecondSeq.add(new OpAssgn(t1, second));
//		saveSecondSeq.add(new OpAssgn(second, new OpDeref(second, next)));
//		saveSecondSeq.add(new OpAssgn(new OpDeref(t1, next), ret));
//		saveSecondSeq.add(new OpAssgn(ret, t1));	
//		saveSecondSeq.add(new OpAssgn(t1, TNull.v));
//		
//		Operator ifFirst = new OpIf(new OpOr(
//											new OpEq(second, TNull.v),
//											new OpAnd(
//													new OpNot(new OpEq(first, TNull.v)),
//													new OpLeq(new OpDeref(second, val), new OpDeref(first, val)))),
//									new OpSeq(saveFirstSeq),
//									new OpSeq(saveSecondSeq));	
//		
//		ArrayList<Operator> seq = new ArrayList<>();
//		seq.add(new OpAssgn(ret, TNull.v));
//		seq.add(new OpWhile(new OpOr(
//									new OpNot(new OpEq(first, TNull.v)),
//									new OpNot(new OpEq(second, TNull.v))),
//							ifFirst));
//		
//		Operator prog = new OpSeq(seq);
//		return prog;
//	}
//
//	public static Trace genMergeLists(int length) {
//		Store input = genMergeListsInput(length);
//		return genMergeLists(input);
//	}
//
//	private static Store genMergeListsInput(int length) {
//		Store empty = new Store();
//		Store input = genSortedSLL(empty, first, length);
//		input = genSortedSLL(input, second, length);
//		input = input.assign(ret, Obj.NULL);
//		input = input.assign(t1, Obj.NULL);
//		return input;
//	}
//
//	public static Trace genMergeLists(Store input) {
//		Operator prog = genMergeListsProgram2();
//		Trace trace = new Trace();
//		Store output = Interpreter.v.apply(prog, input, trace);
//		assert (!(output instanceof ErrorStore));
//		return trace;
//	}
}
