package jminor.codegen;

import java.io.File;
import java.util.logging.Logger;

import org.apache.commons.configuration2.Configuration;
import org.stringtemplate.v4.ST;

import bgu.cs.util.FileUtils;
import bgu.cs.util.STGLoader;
import bgu.cs.util.StringUtils;
import jminor.BooleanType;
import jminor.IntType;
import jminor.JminorDebugger;
import jminor.JminorProblem;
import jminor.RefVar;
import jminor.Var;
import pexyn.grammarInference.Grammar;
import pexyn.grammarInference.Nonterminal;
import pexyn.grammarInference.SententialForm;
import pexyn.grammarInference.Terminal;
import pexyn.grammarInference.StmtLetter;;

public class GrammarCodegen {
	private final Grammar cfg;
	private final JminorProblem problem;
	private final Configuration config;
	private final JminorDebugger debugger;
	private final Logger logger;

	private final STGLoader templates;
	private final String languageName;
	private final String fileSuffix;
	private final SemanticsRenderer semRenderer;

	public static GrammarCodegen forJava(Grammar cfg, JminorProblem problem, Configuration config,
			JminorDebugger debugger, Logger logger) {
		return new GrammarCodegen(cfg, problem, config, debugger, logger, "Java", "java",
				new STGLoader(GrammarCodegen.class, "þþJavaGrammarCodegen.stg"), new JavaSemanticsRenderer());
	}

	public static GrammarCodegen forDafny(Grammar cfg, JminorProblem problem, Configuration config,
			JminorDebugger debugger, Logger logger) {
		return new GrammarCodegen(cfg, problem, config, debugger, logger, "Dafny", "dfy",
				new STGLoader(GrammarCodegen.class, "DafnyGrammarCodegen.stg"), new DafnySemanticsRenderer());
	}

	public GrammarCodegen(Grammar cfg, JminorProblem problem, Configuration config, JminorDebugger debugger,
			Logger logger, String languageName, String fileSuffix, STGLoader templates, SemanticsRenderer semRenderer) {
		this.cfg = cfg;
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
		if (cfg.getCurrStartProduct().size() == 0) {
			logger.info("Encountered degenerate cfg. Skipped code generation.");
			return;
		}
		var className = StringUtils.capitalizeFirst(problem.name);
		var methodName = problem.name;
		var classFileName = className + "." + fileSuffix;
		var classFileST = templates.load("ClassFile");
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

		classFileST.add("functions", render(cfg.getStart()));
		for (var nt : cfg.getNonterminals()) {
			classFileST.add("functions", render(nt));
		}
		renderTransitions(classFileST);

		var text = classFileST.render();
		debugger.info(text);
		setOutputDirectory();
		debugger.addCodeFile(problem.name + "-" + fileSuffix + "-implementation.txt", text, "A " + languageName + " implementation");
		FileUtils.stringToFile(text, config.getString("pexyn.implementationDir", ".") + File.separator + classFileName);
	}
	
	private String render(Nonterminal nt) {
		var message = new StringBuilder();
		var wholefunc = new StringBuilder();
		wholefunc.append("private void Func" + nt.getName() + "(){\n");
		if(nt.getIsRecursive()) {
			message.append("\twhile(" + nt.getGuards().get(0).toString() + "){\n");
			SententialForm body = nt.getProductions().get(0);
			body.remove(body.size() -1);
			String renderedBody = render(body);
			String indented = renderedBody.replaceAll("(?m)^", "\t\t");
			message.append( indented + "\n\t}\n");
		} else if (nt.isIfNt()|| nt.isIfElseNt()){
			message.append("\tif(" + nt.getGuards().get(0).toString() + "){\n");
			var body = render(nt.getProductions().get(0));
			String indented = body.replaceAll("(?m)^", "\t\t");
			message.append(indented + "\n\t}");
			if (nt.isIfElseNt()) {
				message.append(" else {\n");
				body = render(nt.getProductions().get(1));
				indented = body.replaceAll("(?m)^", "\t\t");
				message.append(indented + "\n\t}\n");
			} else {
				message.append("\n");
			}
		} else {
			var body = render(nt.getProductions().get(0));
			String indented = body.replaceAll("(?m)^", "\t");
			message.append(indented);
		}
		wholefunc.append(message.toString().replaceAll("(?m)^", ""));
		wholefunc.append("}\n");
		return wholefunc.toString();
	}

	private String render(SententialForm body) {
		var message = new StringBuilder();
		for(var sym : body) {
			if(sym.getClass() == Nonterminal.class) {
				var nt = (Nonterminal) sym;
				message.append("Func" + nt.getName() + "();\n");
			} else {
				var t = (Terminal) sym;
				var stmt = (StmtLetter) t.id;
				message.append(stmt.toString() + "\n");
			}
		}
		return message.toString();
	}

	private void setOutputDirectory() {
		var outputDirProp = config.getString("pexyn.implementationDir", "output");
		var outputDirFile = new File(outputDirProp);
		outputDirFile.mkdir();
	}	

	private void renderTransitions(ST classFileST) {/*
		for (var state : cfg.getNodes()) {
			if (state == cfg.getFinal()) {
				continue;
			}

			var stateCodeST = templates.load("StateCode");
			stateCodeST.add("name", stateName(state.toString()));
			var trans = new ArrayList<Transition>();
			for (var edge : cfg.succEdges(state)) {
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
		}*/
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

}
