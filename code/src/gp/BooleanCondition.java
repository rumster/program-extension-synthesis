package gp;

public interface BooleanCondition {
	public BooleanCondition not(BooleanCondition sub);

	public BooleanCondition and(BooleanCondition first, BooleanCondition second);

	public BooleanCondition or(BooleanCondition first, BooleanCondition second);
}