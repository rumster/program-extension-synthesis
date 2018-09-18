package pexyn.grammarInference;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import bgu.cs.util.Timer;
import jminor.BasicJminorTR;
import jminor.BoolExpr;
import jminor.JmStore;
import jminor.JminorDebugger;
import jminor.JminorProblem;
import jminor.Stmt;
import jminor.ast.ASTProblem;
import jminor.ast.JminorParser;
import jminor.ast.ProblemCompiler;
import jminor.codegen.GrammarCodegen;
import pexyn.Example;
import pexyn.PETISynthesizer;
import pexyn.Trace;
import pexyn.guardInference.ConditionInferencer;
import pexyn.guardInference.DTreeInferencer;
import pexyn.planning.AStar;


/**
 * Synthesizes programs from a heap-format specification file.
 * 
 * @author romanm
 */
public class Main {
	protected final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	private static final String OUTPUT_DIR_KEY = "pexyn.outputDir";
	private static final String PROPERTIES_FILE_NAME = "pexyn.properties";

	private String outputDirPath = null;

	private File logFile = null;
	private String logFilePath = null;
	
	private Timer inferrenceTime = new Timer();
	private JminorDebugger debugger = null;

	private Configuration config = null;

	private String filename;
	public String[] filenames;
	
	public Main(String[] args) {
		this.filenames = args;
	}

	public static void printUsage() {
		System.err.println("Usage: <files>");
	}
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.format("Error: expected at least one arguement, got 0");
			printUsage();
			return;
		}
		Main main = new Main(args);
		main.run();
	}

	public JminorProblem genProblem() {
		JminorParser parser = new JminorParser();
		ASTProblem root = null;
		try {
			logger.info("Parsing " + filename + "... ");
			root = parser.parseFile(filename);
			logger.info("done");
		} catch (Exception e) {
			throw new Error(e.getMessage());
		}
		logger.info("Compiling... ");
		ProblemCompiler compiler = new ProblemCompiler(root);
		JminorProblem problem = compiler.compile();
		logger.info("done");
		return problem;
	}

	/**
	 * Starts the ball rolling.
	 */
	public void run() {
		//logger.setLevel(Level.OFF);
		var configs = new Configurations();
		try {
			config = configs.properties(new File(PROPERTIES_FILE_NAME));
			outputDirPath = config.getString(OUTPUT_DIR_KEY, "./");
			var dir = new File(outputDirPath);
			outputDirPath = dir.getAbsolutePath();
			dir.mkdirs();
		} catch (ConfigurationException cex) {
			logger.severe("Initialization failed: unable to load " + PROPERTIES_FILE_NAME + "!");
			return;
		}
		setOutputDirectory();
		debugger = new JminorDebugger(config, logger, filename, outputDirPath);
		for(String file: filenames) {
			this.filename = file;
			inferrenceTime.reset();
			try {
				JminorProblem problem = genProblem();
				setOutLogFile(problem.name);
				logger.info("Synthesizer: started");
				debugger.addLink(problem.name + "Events.txt", problem.name + " Events log");
				debugger.addCodeFile(problem.name + "Problem.txt", problem.toString(), problem.name + " Specification");
				debugger.printExamples(problem.examples);
				var planner = new AStar<JmStore, Stmt>(new BasicJminorTR(problem.semantics));
				var synthesizer = new PETISynthesizer<JmStore, Stmt, BoolExpr>(planner, config, debugger);
				var plans = synthesizer.genPlans(problem);
				
				var exampleToPlan = plans;
				var trainingPlans = new ArrayList<Trace<JmStore, Stmt>>();
				exampleToPlan.forEach((example, plan) -> {
					if (!example.isTest) {
						trainingPlans.add(plan);
						debugger.info("Example " + example.name + ". Plan length = " + plan.size());
					}
				});

				ConditionInferencer<JmStore, Stmt, BoolExpr> separator;
				var shortCiruitEvaluationSemantics = config.getBoolean("pexyn.shortCiruitEvaluationSemantics", true);
				var basicGuards = problem.semantics().generateBasicGuards(trainingPlans);
				separator = new DTreeInferencer<JmStore, Stmt, BoolExpr>(problem.semantics(), basicGuards,
						shortCiruitEvaluationSemantics);
				
				inferrenceTime.start();
				var optCFG = synthesizeGrammar(trainingPlans, separator);
				if(!optCFG.isPresent()) {
					debugger.warning("SeqVer: failed to find program CFG!");	
				} else {
					var CFG = optCFG.get();
					debugger.addCodeFile(problem.name + "CFG", CFG.toString(), problem.name + " Synthesis CFG");
					validateExamples(CFG, plans, problem);
					if (config.getBoolean("jminor.generateJavaImplementation", true)) {
						GrammarCodegen backend = GrammarCodegen.forJava(CFG, problem, config, debugger, logger);
						backend.generate();
					}
				}


			} catch (Throwable t) {
				debugger.severe(t.toString());
				t.printStackTrace();
			} finally {
				inferrenceTime.stop();
				logger.info("Synthesizer: done! (" + inferrenceTime.toSeconds() + ")" +
						"\r\n-------------------------------------------------------------\n\n");
				clearOutLogFile();
			}
		}
	}

	private Optional<Grammar> synthesizeGrammar(ArrayList<Trace<JmStore, Stmt>> trainingPlans,
			ConditionInferencer<JmStore, Stmt, BoolExpr> separator) {
		Sequential x = new Sequential();
		x.setSeperator(separator);
		Grammar stableGrammar = new Grammar();
		Trace<JmStore, Stmt> stableExample = null;

		var entries = trainingPlans;
		for(Trace<JmStore, Stmt> val: entries) {
			ArrayList<StmtLetter> actionTrace = new ArrayList<StmtLetter>();
			for(int i=0; i<val.size() -1; i++) {
				var letter = new StmtLetter(val.actionAt(i));
				letter.state = val.stateAt(i);
				actionTrace.add(letter);
			}
 			Grammar currGrammar = x.addExample(actionTrace);
			if (!currGrammar.equals(stableGrammar)) {
				stableGrammar = new Grammar(currGrammar);
				stableExample = val;
			}
		}
		logger.info("Converging trace with " + entries.indexOf(stableExample) + " out of " + entries.size() +" :");
		x.clearStates();
		boolean converged = x.endInput();
		if(!converged) {
			logger.info("CFG STRUCTURE CONVERGENCE FAILED");
			return Optional.empty();
		}
		stableGrammar = x.grammar;
		for(int i=0; i<entries.size(); i++) {
			Trace<JmStore, Stmt> val = entries.get(i);
			ArrayList<StmtLetter> actionTrace = new ArrayList<StmtLetter>();
			for(int j=0; j<val.size() -1; j++) {
				var letter = new StmtLetter(val.actionAt(j));
				letter.state = val.stateAt(j);
				actionTrace.add(letter);
			}
 			Grammar currGrammar = x.addExampleStates(actionTrace);
 			assert( currGrammar.equals(stableGrammar));
 			stableGrammar = currGrammar;
		}
		x.assignGuards();
		stableGrammar = x.grammar;
		logger.info("Final grammar:\n" + stableGrammar.toString());
		return Optional.of(stableGrammar);
	}

	private boolean validateExamples(Grammar cfg, Map<Example<JmStore, Stmt>, Trace<JmStore, Stmt>> exampleToPlan, JminorProblem problem) {
		var message = new StringBuilder();
		var result = true;
		var numOfTests = 0;
		var numOfTestsSucceeded = 0;
		var exampleToCompareResult = new HashMap<Example<JmStore, Stmt>, Boolean>();
		for (var entry : exampleToPlan.entrySet()) {
			var example = entry.getKey();
			var plan = entry.getValue();
			if (!example.isTest) {
				continue;
			}
			++numOfTests;
			var interpreter = new CFGInterpreter(cfg, problem.semantics());
			Optional<Trace<JmStore, Stmt>> optAutomatonTrace = interpreter.genTrace(example.input(), 500);
			if (!optAutomatonTrace.isPresent() || !optAutomatonTrace.get().eqDeterministic(plan)) {
				{
					if (optAutomatonTrace.isPresent()){
						debugger.info("Difference in synthesized result on example " + example.name);
					}
				}
				exampleToCompareResult.put(example, Boolean.FALSE);
				message.append("Testing example " + example.name + ": fail" + System.lineSeparator());
				result = false;
			} else {
				message.append("Testing example " + example.name + ": success" + System.lineSeparator());
				exampleToCompareResult.put(example, Boolean.TRUE);
				++numOfTestsSucceeded;
			}
		}
		message.append("Succeeded on " + numOfTestsSucceeded + " out of " + numOfTests + " test examples.");
		debugger.addCodeFile(problem.name + "Validation", message.toString(), problem.name + " Synthesis test results");
		debugger.info("Succeeded on " + numOfTestsSucceeded + " out of " + numOfTests + " test examples.");
		return result;

		
	}

	@SuppressWarnings("unused")
	private void testConvergence(Map<Example<JmStore, Stmt>, Trace<JmStore, Stmt>> map,
			ConditionInferencer<JmStore, Stmt, BoolExpr> separator) {
		synthesizeGrammar(new ArrayList<>(map.values()), separator);
	}
	private void setOutputDirectory() {
		var outputDirProp = config.getString("pexyn.outputDir", "output");
		var outputDirFile = new File(outputDirProp);
		outputDirFile.mkdir();
		String outputDirPath;
		try {
			outputDirPath = outputDirFile.getCanonicalPath();
			config.setProperty(OUTPUT_DIR_KEY, outputDirPath);
		} catch (IOException e) {
			logger.severe(
					"Unable to set up output directory: " + outputDirFile.getName() + " (" + e.getMessage() + ")");
		}
	}

	private void clearOutLogFile() {
		for(var h: logger.getHandlers()) {
			logger.removeHandler(h);
		}
	}
	private void setOutLogFile(String name) {
		try {
			logFile = new File(outputDirPath + File.separator + name + "Events.txt" );
			logFilePath = logFile.getCanonicalPath();
			FileHandler logFileHandler = new FileHandler(logFilePath);
			logFileHandler.setFormatter(new SimpleFormatter());
			logger.addHandler(logFileHandler);
		} catch (SecurityException | IOException e) {
			logger.severe("Unable to set log file: " + e.getMessage() + "!");
		}
	}
}