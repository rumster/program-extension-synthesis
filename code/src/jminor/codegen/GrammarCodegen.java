package jminor.codegen;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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

	public static GrammarCodegen forJava(Grammar cfg, JminorProblem problem, Configuration config,
			JminorDebugger debugger, Logger logger) {
		return new GrammarCodegen(cfg, problem, config, debugger, logger, "Java", "java",
				new STGLoader(GrammarCodegen.class, "þþJavaGrammarCodegen.stg"));
	}

	public static GrammarCodegen forDafny(Grammar cfg, JminorProblem problem, Configuration config,
			JminorDebugger debugger, Logger logger) {
		return new GrammarCodegen(cfg, problem, config, debugger, logger, "Dafny", "dfy",
				new STGLoader(GrammarCodegen.class, "DafnyGrammarCodegen.stg"));
	}

	public GrammarCodegen(Grammar cfg, JminorProblem problem, Configuration config, JminorDebugger debugger,
			Logger logger, String languageName, String fileSuffix, STGLoader templates) {
		this.cfg = cfg;
		;
		this.problem = problem;
		this.config = config;
		this.debugger = debugger;
		this.logger = logger;
		this.templates = templates;
		this.languageName = languageName;
		this.fileSuffix = fileSuffix;
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

		var text = classFileST.render();
		setOutputDirectory();
		debugger.addCodeFile(problem.name + "-" + fileSuffix + "-implementation.txt", text, problem.name  + " " + languageName + " implementation");
		FileUtils.stringToFile(text, config.getString("pexyn.implementationDir", ".") + File.separator + classFileName);
	}
	
	private String render(Nonterminal nt) {
		var message = new StringBuilder();
		var wholefunc = new StringBuilder();
		List<String> guards = new ArrayList<String>();
		for(int i=0; i<10; i++) guards.add(nt.getGuards().size() > i ? nt.getGuards().get(i).toString() : "?");
		wholefunc.append("private void Func" + nt.getName() + "(){\n");
		if(nt.getIsRecursive()) {
			message.append("\twhile(" + guards.get(0).toString() + "){\n");
			SententialForm body = nt.getProductions().get(0);
			body.remove(body.size() -1);
			String renderedBody = render(body);
			String indented = renderedBody.replaceAll("(?m)^", "\t\t");
			message.append( indented + "\n\t}\n");
		} else if (nt.isIfNt()|| nt.isIfElseNt()){
			message.append("\tif(" + guards.get(0).toString() + "){\n");
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
