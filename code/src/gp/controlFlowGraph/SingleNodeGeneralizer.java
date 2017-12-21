package gp.controlFlowGraph;

import java.util.Collection;

import gp.GPDebugger;
import gp.Plan;

public class SingleNodeGeneralizer<StateType, ActionType, ConditionType>
		extends CFGGeneralizer<StateType, ActionType, ConditionType> {
	protected final GPDebugger<StateType, ActionType, ConditionType> debugger;

	public SingleNodeGeneralizer(GPDebugger<StateType, ActionType, ConditionType> debugger) {
		this.debugger = debugger;
	}

	@Override
	public Result generalize(Collection<Plan<StateType, ActionType>> plans,
			CFG<StateType, ActionType, ConditionType> cfg) {
		int index = 0;
		for (Plan<StateType, ActionType> plan : plans) {
			CFGUtils.addPlanToCFG(cfg, plan);
			debugger.printGraph(cfg, "After adding plan " + index++);
		}

		return Result.CONDITION_INFERENCE_FAILURE;
	}
}