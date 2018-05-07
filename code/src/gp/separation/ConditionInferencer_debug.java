package gp.separation;

import java.util.Collection;
import java.util.Optional;

import gp.Domain;
import gp.Domain.Value;
import heap.BoolExpr;
import heap.EqExpr;
import heap.NotExpr;
import heap.NullExpr;
import heap.RefType;
import heap.RefVar;
import heap.Stmt;
import heap.Store;
import heap.Var.VarRole;

/**
 * Infers conditions for split nodes in the CFG.
 */
public class ConditionInferencer_debug extends ConditionInferencer<Store, Stmt, BoolExpr> {
	public ConditionInferencer_debug(Domain<Store, Stmt, BoolExpr> domain) {
		super(domain);
	}

	@Override
	public Optional<BoolExpr> infer(Collection<? extends Value> first, Collection<? extends Value> second) {
		BoolExpr result = null;

		RefType sllType = new RefType("SLL");
		RefVar head = new RefVar("head", sllType, VarRole.ARG, true, false);

		// head == NULL
		BoolExpr condT = new EqExpr(head, NullExpr.v);
		BoolExpr condF = new NotExpr(condT);

		if (testCondition(condT, first, true) && testCondition(condF, second, true)) {
			result = condT;
		} else if (testCondition(condF, first, true) && testCondition(condT, second, true)) {
			result = condF;
		}
		return Optional.of(result);
	}

	private boolean testCondition(BoolExpr condition, Collection<? extends Value> states, boolean expected) {
		for (Value v : states) {
			Store s = (Store) v;
			if (domain.test(condition, s) != expected) {
				return false;
			}
		}
		return true;
	}

	//
	// /**
	// * Adds conditions to the actions labeling CFG edges such that any two
	// * actions originating from the same CFG node are separated (are not enabled
	// * simultaneously).
	// *
	// * TODO: conditions should be nullderef-protected.
	// *
	// * @return true if conditions have been successfully inferred.
	// */
	// public boolean inferConditions(CFG cfg) {
	// boolean foundConditions = true;
	// for (CFGNode node : cfg.getNodes()) {
	// //entry node has no states, there is no meaning for separating conditions
	// if (cfg.outDegree(node) < 2)
	// //|| node.equals(cfg.ENTRY))
	// continue;
	// boolean foundSeparator = inferConditionsForNode(cfg, node);
	// if (!foundSeparator)
	// Globals.LOGGER.info("failed to infer cond for node " + node);
	// foundConditions &= foundSeparator;
	// }
	// return foundConditions;
	// }
	//
	//
	// public float conditionsCost(CFG cfg) {
	// float totalCost = 0;
	// for (CFGNode node : cfg.getNodes()) {
	// if (cfg.outDegree(node) < 2)
	// continue;
	// float nodeCost = conditionsCostForNode(cfg, node);
	// totalCost += nodeCost; //??? nodeCost^2 or nodeCost*factor
	// }
	// return totalCost;
	// }
	//
	// public float conditionsCostForNode(CFG cfg, CFGNode node) {
	// float totalCost = 0;
	// int outDegree = cfg.outDegree(node);
	// for (MultiGraph.Edge<CFGNode, Action> edge : cfg.succEdges(node)) {
	// Action action = edge.getLabel();
	// if((outDegree >= 2) && (action.guard == null)) {
	// // && !node.equals(cfg.ENTRY)){
	// // a node with out-degree >= 2 must have conditions on all the out-edges
	// // otherwise it is in invalid state (most likely condition inferencer failed
	// on some edge)
	// return CostFun.INFINITY_COST;
	// }
	// else{
	// totalCost += cost1.apply(action.guard, CostFun.INFINITY_COST);
	// }
	// }
	// return totalCost;
	// }
	//
	// private void cleanConditionsForNode(CFG cfg, CFGNode node) {
	// for (MultiGraph.Edge<CFGNode, Action> edge : cfg.succEdges(node)) {
	// Action action = edge.getLabel();
	// action.guard = null;
	// }
	// }
	//
	// public boolean inferConditionsForNode(CFG cfg, CFGNode node) {
	// cleanConditionsForNode(cfg, node);
	//
	// HashRel2<Operator, State> operatorToStates = new HashRel2<>();
	//
	// for (State state : node.states) {
	// for (MultiGraph.Edge<State, Label<Operator>> outTransition :
	// cfg.transitions.succEdges(state)) {
	// CFG.Label<Operator> l = outTransition.getLabel();
	// if(node.getLables().contains(l)){
	// Operator op = l.getValue();
	// operatorToStates.add(op, state);
	// }
	// }
	// }
	//
	// // Find a condition for each outgoing edge.
	// for (MultiGraph.Edge<CFGNode, Action> edge : cfg.succEdges(node)) {
	// Action action = edge.getLabel();
	// while(true){
	// List<State> thisTerm = new ArrayList<>();
	// List<State> othersTerm = new ArrayList<>();
	// for (MultiGraph.Edge<CFGNode, Action> otherEdge : cfg.succEdges(node)) {
	// Action otherAction = otherEdge.getLabel();
	// Collection<State> otherEdgeStates =
	// operatorToStates.select1(otherAction.update);
	//
	// List<State> cur;
	// if(edge != otherEdge){
	// cur = othersTerm;
	// }
	// else{
	// cur = thisTerm;
	// }
	// cur.addAll(otherEdgeStates);
	// }
	//
	// TRefType sllType = new TRefType("SLL");
	// TIntField val = new TIntField("d", sllType);
	// TRefVar first = new TRefVar("first", sllType, VarRole.ARG, false, false);
	// TRefVar second = new TRefVar("second", sllType, VarRole.ARG, false, false);
	//
	// List<Operator> conditions = new ArrayList<>();
	//
	// // 1) first != null || second != null
	// Operator cond1 = new OpOr(
	// new OpNot(new OpEq(first, TNull.v)),
	// new OpNot(new OpEq(second, TNull.v)));
	// conditions.add(cond1);
	//
	//
	// // 2) (first != null || second != null) && (second == null || first != null
	// && first.d >= second.d)
	// Operator cond2 = new OpAnd(cond1,
	// new OpOr(
	// new OpEq(second, TNull.v),
	// new OpAnd(
	// new OpNot(new OpEq(first, TNull.v)),
	// new OpLeq(new OpDeref(second, val), new OpDeref(first, val)))));
	// conditions.add(cond2);
	//
	// // 3) (first != null || second != null) && !(second == null || first != null
	// && first.d >= second.d)
	// Operator cond3 = new OpAnd(cond1,
	// new OpNot(
	// new OpOr(
	// new OpEq(second, TNull.v),
	// new OpAnd(
	// new OpNot(new OpEq(first, TNull.v)),
	// new OpLeq(new OpDeref(second, val), new OpDeref(first, val))))));
	// conditions.add(cond3);
	//
	//// boolean found = false;
	//// if (testCondition(cond1, thisTerm, true) && testCondition(cond2,
	// othersTerm, true)) {
	//// found = true;
	//// action.guard = cond1;
	//// }
	//// else if (testCondition(cond2, thisTerm, true) && testCondition(cond1,
	// othersTerm, true)) {
	//// found = true;
	//// action.guard = cond2;
	//// }
	//// else if (testCondition(cond3, thisTerm, true) && testCondition(cond4,
	// othersTerm, true)) {
	//// found = true;
	//// action.guard = cond3;
	//// }
	//// else if (testCondition(cond4, thisTerm, true) && testCondition(cond3,
	// othersTerm, true)) {
	//// found = true;
	//// action.guard = cond4;
	//// }
	//
	// boolean found = false;
	// for(Operator op : conditions) {
	// Operator conditionT = op;
	// Operator conditionF = new OpNot(conditionT);
	// if (testCondition(conditionT, thisTerm, true)) {
	// found = testCondition(conditionT, othersTerm, false);
	// if(found) {
	// action.guard = conditionT;
	// break;
	// }
	// }
	// else if(testCondition(conditionF, thisTerm, true)) {
	// found = testCondition(conditionF, othersTerm, false);
	// if(found) {
	// action.guard = conditionF;
	// break;
	// }
	// }
	// }
	//
	//
	//// boolean found = false;
	//// for(Operator op : conditions) {
	//// Operator conditionT = op;
	//// Operator conditionF = new OpNot(conditionT);
	//// if (testCondition(conditionT, thisTerm, true)) {
	//// found = testCondition(conditionT, othersTerm, false);
	//// if(found) {
	//// action.guard = conditionT;
	//// break;
	//// }
	//// }
	//// else if(testCondition(conditionF, thisTerm, true)) {
	//// found = testCondition(conditionF, othersTerm, false);
	//// if(found) {
	//// action.guard = conditionF;
	//// break;
	//// }
	//// }
	//// }
	// if(!found) {
	// return false;
	// }
	// else {
	// break;
	// }
	// }
	// }
	// return true;
	// }
	//
	// protected static boolean testCondition(Operator condition, Collection<State>
	// states, boolean expected) {
	// for (State s : states) {
	// Boolean testResult = Interpreter.v.test(condition, s);
	// if (testResult == null) {
	// // Meaning a nullderef has occurred, which we count the
	// // same as the cond not holding. (We implicitly
	// // assume each access path is nullderef-protected.)
	// return false;
	// }
	// if (testResult.booleanValue() != expected) {
	// return false;
	// }
	// }
	// return true;
	// }
}
