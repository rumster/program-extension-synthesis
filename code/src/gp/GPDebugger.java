package gp;

import java.util.logging.Logger;

import bgu.cs.util.HTMLPrinter;

/**
 * A debugger for generalized planning.
 */
public abstract class GPDebugger<StateType, ActionType, ConditionType> extends HTMLPrinter {
	public GPDebugger(Logger logger, String title, String outputDirPath) {
		super(logger, title, outputDirPath);
	}

	/**
	 * Prints a plan.
	 */
	public abstract void printPlan(Plan<StateType, ActionType> plan, int planIndex);
}