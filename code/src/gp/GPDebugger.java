package gp;

import java.util.logging.Logger;

import bgu.cs.util.HTMLPrinter;

/**
 * A debugger for generalized planning.
 */
public abstract class GPDebugger<StateType, ActionType, ConditionType> extends HTMLPrinter {
	/**
	 * Constructs a debugger for generalized planning tasks.
	 * 
	 * @param logger
	 *            The logger to which messages are sent to.
	 * @param title
	 *            The title of the web-page displaying information.
	 * @param outputDirPath
	 *            The directory in which display-related files are generated.
	 */
	public GPDebugger(Logger logger, String title, String outputDirPath) {
		super(logger, title, outputDirPath);
	}

	/**
	 * Prints a plan.
	 */
	public abstract void printPlan(Plan<StateType, ActionType> plan, int planIndex);
}