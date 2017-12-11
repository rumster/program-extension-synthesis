package gp.cfgGeneralization;

import java.util.Collection;

import gp.CFG;
import gp.CFGGeneralizer;
import gp.Plan;

public class SingleNodeGeneralizer<StateType, ActionType, ConditionType>
		extends CFGGeneralizer<StateType, ActionType, ConditionType> {

	@Override
	public Result generalize(Collection<Plan<StateType, ActionType>> plans, CFG<ActionType, ConditionType> result) {
		return Result.CONDITION_INFERENCE_FAILURE;
	}
}