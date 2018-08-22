package jminor.codegen;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.commons.configuration2.Configuration;
import org.stringtemplate.v4.ST;

import bgu.cs.util.FileUtils;
import bgu.cs.util.STGLoader;
import bgu.cs.util.StringUtils;
import bgu.cs.util.graph.MultiGraph.Edge;
import jminor.BooleanType;
import jminor.IntType;
import jminor.JminorDebugger;
import jminor.JminorProblem;
import jminor.RefVar;
import jminor.Var;
import pexyn.generalization.Action;
import pexyn.generalization.Automaton;
import pexyn.generalization.State;

/**
 * Generates a Java implementation from an automaton.<br>
 * TODO: generate the class definitions for Dafny.
 * 
 * @author romanm
 */
public class AutomatonCodegen {
	private final Automaton automaton;
	private final JminorProblem problem;
	private final Configuration config;
	private final JminorDebugger debugger;
	private final Logger logger;

	private final STGLoader templates;
	private final String languageName;
	private final String fileSuffix;
	private final SemanticsRenderer semRenderer;

	public static AutomatonCodegen forJava(Automaton automaton, JminorProblem problem, Configuration config,
			JminorDebugger debugger, Logger logger) {
		return new AutomatonCodegen(automaton, problem, config, debugger, logger, "Java", "java",
				new STGLoader(AutomatonCodegen.class, "JavaAutomatonCodegen.stg"), new JavaSemanticsRenderer());
	}

	public static AutomatonCodegen forDafny(Automaton automaton, JminorProblem problem, Configuration config,
			JminorDebugger debugger, Logger logger) {
		return new AutomatonCodegen(automaton, problem, config, debugger, logger, "Dafny", "dfy",
				new STGLoader(AutomatonCodegen.class, "DafnyAutomatonCodegen.stg"), new DafnySemanticsRenderer());
	}

	public AutomatonCodegen(Automaton automaton, JminorProblem problem, Configuration config, JminorDebugger debugger,
			Logger logger, String languageName, String fileSuffix, STGLoader templates, SemanticsRenderer semRenderer) {
		this.automaton = automaton;
		;
		this.problem = problem;
		this.config = config;
		this.debugger = debugger;
		this.logger = logger;
		this.templates = templates;
		this.languageName = languageName;
		this.fileSuffix = fileSuffix;
		this.semRenderer = semRenderer;
	}

	public void generate() {
		if (automaton.outDegree(automaton.getInitial()) == 0) {
			logger.info("Encountered degenerate automaton. Skipped code generation.");
			return;
		}
		var className = StringUtils.capitalizeFirst(problem.name);
		var methodName = problem.name;
		var classFileName = className + "." + fileSuffix;
		var classFileST = isDegenerateAutomaton() ? templates.load("SimpleClassFile") : templates.load("ClassFile");
		classFileST.add("className", className);
		classFileST.add("methodName", methodName);
		for (var inputArg : problem.inputArgs) {
			classFileST.add("args", new JavaVar(inputArg));
		}
		for (var outputArg : problem.outputArgs) {
			classFileST.add("returnArg", new JavaVar(outputArg));
		}
		for (var temp : problem.temps) {
			classFileST.add("locals", new JavaVar(temp));
		}

		if (isDegenerateAutomaton()) {
			var cmd = automaton.succEdges(automaton.getInitial()).iterator().next().label.update;
			classFileST.add("stateCodes", semRenderer.renderCmd(cmd));
		} else {
			for (var state : automaton.getNodes()) {
				classFileST.add("states", stateName(state.toString()));
			}
			renderTransitions(classFileST);
		}

		var text = classFileST.render();
		setOutputDirectory();
		debugger.addCodeFile(fileSuffix + "-implementation.txt", text, "A " + languageName + " implementation");
		FileUtils.stringToFile(text, config.getString("pexyn.implementationDir", ".") + File.separator + classFileName);
	}
	
	private void setOutputDirectory() {
		var outputDirProp = config.getString("pexyn.implementationDir", "output");
		var outputDirFile = new File(outputDirProp);
		outputDirFile.mkdir();
	}	

	/**
	 * Checks whether the automaton consists of a single transition (meaning that
	 * automaton compression successfully compressed the entire transition relation
	 * into a single compound statement).
	 */
	private boolean isDegenerateAutomaton() {
		return automaton.getNodes().size() == 2 && automaton.degree(automaton.getInitial()) == 1;
	}

	private void renderTransitions(ST classFileST) {
		for (var state : automaton.getNodes()) {
			if (state == automaton.getFinal()) {
				continue;
			}

			var stateCodeST = templates.load("StateCode");
			stateCodeST.add("name", stateName(state.toString()));
			var trans = new ArrayList<Transition>();
			for (var edge : automaton.succEdges(state)) {
				trans.add(new Transition(edge));
			}
			if (trans.size() == 1) {
				trans.get(0).type = TransitionType.UPDATE;
			} else {
				trans.get(0).type = TransitionType.FIRST;
				for (int i = 1; i < trans.size() - 1; ++i) {
					trans.get(i).type = TransitionType.MIDDLE;
				}
				trans.get(trans.size() - 1).type = TransitionType.LAST;
			}
			for (var transition : trans) {
				ST transST = null;
				switch (transition.type) {
				case UPDATE:
					transST = templates.load("UpdateTransition");
					break;
				case FIRST:
					transST = templates.load("IfTransition");
					transST.add("guard", transition.guard);
					break;
				case MIDDLE:
					transST = templates.load("ElseIfTransition");
					transST.add("guard", transition.guard);
					break;
				case LAST:
					transST = templates.load("ElseTransition");
					break;
				}
				transST.add("update", transition.command);
				transST.add("succ", transition.succ);
				stateCodeST.add("transitions", transST.render());
			}
			classFileST.add("stateCodes", stateCodeST.render());
		}
	}

	public static enum TransitionType {
		UPDATE, FIRST, MIDDLE, LAST
	}

	public final class Transition {
		public final String succ;
		public final String guard;
		public final String command;
		public TransitionType type;

		public Transition(Edge<State, Action> e) {
			this.succ = stateName(e.getDst().toString());
			var guard = e.getLabel().guard();
			if (guard != null) {
				this.guard = semRenderer.renderGuard(guard);
			} else {
				this.guard = null;
			}
			var update = e.getLabel().update;
			if (update.toString().equals("return")) {
				this.command = "// finish";
			} else {
				this.command = semRenderer.renderCmd(update);
			}
		}
	}

	public final class JavaVar {
		public final String name;
		public final String defaultVal;
		private final String type;

		public String getType() {
			ST nonNullTypeST = templates.load("NonNullType");
			nonNullTypeST.add("type", this.type);
			return nonNullTypeST.render();
		}

		// TODO: clean this up - get the default value directly from the type.
		public JavaVar(Var v) {
			this.name = v.name;
			this.type = v.getType().getName();
			if (v.getType() instanceof IntType) {
				this.defaultVal = "0";
			} else if (v.getType() instanceof BooleanType) {
				this.defaultVal = "false";
			} else if (v instanceof RefVar) {
				this.defaultVal = "null";
			} else {
				throw new Error("Unsupported variable type: " + v);
			}
		}
	}

	public static String stateName(String originalName) {
		if (originalName.equals("initial")) {
			return "ENTRY";
		} else if (originalName.equals("final")) {
			return "EXIT";
		} else {
			return originalName;
		}
	}
}
