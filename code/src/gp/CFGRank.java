package gp;

public interface CFGRank<StateType, ActionType, ConditionType> {
	public float rank(CFG<StateType, ActionType, ConditionType> cfg);
}