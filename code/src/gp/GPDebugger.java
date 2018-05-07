package gp;

import java.util.logging.Logger;

import bgu.cs.util.HTMLPrinter;
import bgu.cs.util.graph.MultiGraph.Edge;
import bgu.cs.util.graph.visualization.EdgeDataProperties;
import bgu.cs.util.graph.visualization.GraphicProperties;
import bgu.cs.util.graph.visualization.NodeProperties;
import gp.Domain.Guard;
import gp.Domain.Update;
import gp.Domain.Value;
import gp.tmti.Action;
import gp.tmti.Automaton;
import gp.tmti.State;

/**
 * A debugger for generalized planning.
 */
public abstract class GPDebugger<ValueType extends Value, UpdateType extends Update, GuardType extends Guard>
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
		super(logger, title, outputDirPath);
	}

	/**
	 * Prints a plan.
	 */
	public abstract void printPlan(Plan<ValueType, UpdateType> plan, int planIndex);

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

		printGraph(automaton, description, automatonProps);
	}

	public abstract String renderUpdate(Update update);

	public abstract String renderGuard(Guard guard);
}