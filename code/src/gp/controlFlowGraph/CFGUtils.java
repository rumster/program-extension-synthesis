package gp.controlFlowGraph;

import gp.Plan;

public class CFGUtils {
	public static <StateType, ActionType, ConditionType> void addPlanToCFG(
			CFG<StateType, ActionType, ConditionType> cfg, Plan<StateType, ActionType> plan) {
		assert plan.size() >= 2;

		CFG.Node current = CFG.ENTRY;
		cfg.nodeToState.add(current, plan.firstState());

		for (int i = 0; i < plan.size() - 1; ++i) {
			CFG.Node next;
			if (i < plan.size() - 2) {
				next = new CFG.Node();
				cfg.addNode(next);
			} else {
				next = CFG.EXIT;
			}
			cfg.nodeToState.add(next, plan.stateAt(i + 1));

			CFG.ConditionalAction<ActionType, ConditionType> conAction = new CFG.ConditionalAction<>(plan.actionAt(i));
			cfg.addEdge(current, next, conAction);

			current = next;
		}
	}
}