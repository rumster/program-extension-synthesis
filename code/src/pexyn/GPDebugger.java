package pexyn;

import java.util.logging.Logger;

import bgu.cs.util.HTMLPrinter;
import bgu.cs.util.graph.MultiGraph.Edge;
import bgu.cs.util.graph.visualization.EdgeDataProperties;
import bgu.cs.util.graph.visualization.GraphicProperties;
import bgu.cs.util.graph.visualization.NodeProperties;
import pexyn.Semantics.Guard;
import pexyn.Semantics.Cmd;
import pexyn.Semantics.Store;
import pexyn.generalization.Action;
import pexyn.generalization.Automaton;
import pexyn.generalization.State;

/**
 * A debugger for generalized planning.
 */
public abstract class GPDebugger<StoreType extends Store, CmdType extends Cmd, GuardType extends Guard>
		extends HTMLPrinter {
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
		super(title, outputDirPath, logger);
	}

	/**
	 * Prints a plan.
	 */
	public abstract void printPlan(Trace<StoreType, CmdType> plan, int planIndex);

	/**
	 * Prints the given automaton with the given description.
	 */
	public void printAutomaton(Automaton automaton, String description) {
		var automatonProps = new GraphicProperties<State, Action>();
		for (State state : automaton.getNodes()) {
			var stateProps = new NodeProperties(state.id);
			if (state == automaton.getFinal()) {
				stateProps.style = "peripheries=2";
			}
			automatonProps.setProp(state, stateProps);
			for (Edge<State, Action> edge : automaton.succEdges(state)) {
				Action action = edge.getLabel();
				// Avoid printing trivial guards.
				var trivialGuard = automaton.outDegree(state) == 1 || action.guard().toString().equals("true");
				String guardStr = trivialGuard ? "" : renderGuard(action.guard()) + "/\n";
				String actionStr = guardStr + renderUpdate(action.update);
				var edProps = new EdgeDataProperties(actionStr);
				automatonProps.setProp(action, edProps);
			}
		}

		addGraph(automaton, description, automatonProps);
	}

	/**
	 * Returns a textual representation for the given update.
	 */
	public abstract String renderUpdate(Cmd update);

	/**
	 * Returns a textual representation for the given guard.
	 */
	public abstract String renderGuard(Guard guard);
}