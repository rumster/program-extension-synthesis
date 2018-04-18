package gp.controlFlowGraph;

import java.util.Collection;

import gp.GPDebugger;
import gp.Plan;

public class TMTI<StateType, ActionType, ConditionType> extends CFGGeneralizer<StateType, ActionType, ConditionType> {
	private final GPDebugger<StateType, ActionType, ConditionType> debugger;
	
	public TMTI(GPDebugger<StateType, ActionType, ConditionType> debugger) {
		this.debugger = debugger;
	}

	@Override
	public Result generalize(Collection<Plan<StateType, ActionType>> plans,
			CFG<StateType, ActionType, ConditionType> result) {
		return Result.OK;
	}
}
