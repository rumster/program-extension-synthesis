package jminor;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import bgu.cs.util.Timer;
import jminor.ast.ASTProblem;
import jminor.ast.JminorParser;
import jminor.ast.ProblemCompiler;
import pexyn.PETISynthesizer;
import pexyn.generalization.AutomatonOps;
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

	private Timer synthesisTime = new Timer();
	private Timer planningTime = new Timer();

	private File logFile = null;
	private String logFilePath = null;

	private JminorDebugger debugger = null;

	private Configuration config = null;

	private final String filename;

	public Main(String filename) {
		this.filename = filename;
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			throw new Error("Expected a file name!");
		}
		String filename = args[0];
		Main main = new Main(filename);
		main.run();
	}

	public JminorProblem genProblem() {
		JminorParser parser = new JminorParser();
		ASTProblem root = null;
		try {
			System.out.print("Parsing " + filename + "... ");
			root = parser.parseFile(filename);
			System.out.println("done");
		} catch (Exception e) {
			throw new Error(e.getMessage());
		}
		System.out.print("Compiling... ");
		ProblemCompiler compiler = new ProblemCompiler(root);
		JminorProblem problem = compiler.compile();
		System.out.println("done");
		return problem;
	}

	/**
	 * Starts the ball rolling.
	 */
	public void run() {
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
		logger.info("Synthesizer: started");
		synthesisTime.reset();
		planningTime.reset();
		try {
			var problem = genProblem();
			debugger = new JminorDebugger(config, logger, problem.name, outputDirPath);
			debugger.addLink(logFile.getName(), "Events log");
			debugger.addCodeFile("problem.txt", problem.toString(), "Specification");
			debugger.printExamples(problem.examples);
			synthesisTime.start();
			var planner = new AStar<JmStore, Stmt>(new BasicJminorTR(problem.semantics));
			var synthesizer = new PETISynthesizer<JmStore, Stmt, BoolExpr>(planner, config, logger, debugger);
			var synthesisResult = synthesizer.synthesize(problem);
			if (synthesisResult.success()) {
				logger.info("success!");
				if (config.getBoolean("jminor.generateJavaImplementation", true)) {
					// We have to shrink _after_ testing against the test examples,
					// since currently a command sequence is counted as an atomic
					// command, which fails the tests.
					var automaton = synthesisResult.get();
					if (config.getBoolean("pexyn.shrinkResultAutomaton", false)) {
						AutomatonOps.shrinkBlocks(automaton, problem.semantics());
						debugger.printAutomaton(automaton, "Shrunk automaton");
					}
					var backend = new jminor.java.AutomatonBackend(automaton, problem, config, debugger);
					backend.generate();
					var dfYbackend = new jminor.dafny.AutomatonBackend(automaton, problem, config, debugger);
					dfYbackend.generate();
				}
			} else {
				logger.info("fail!");
			}
		} catch (Throwable t) {
			logger.severe(t.toString());
			t.printStackTrace();
		} finally {
			synthesisTime.stop();
			logger.info("Planning time: " + planningTime.toSeconds());
			logger.info("Synthesizer: done! (" + synthesisTime.toSeconds() + ")");
			debugger.refresh();
		}
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
		setOutLogFile();
	}

	private void setOutLogFile() {
		try {
			logFile = new File(outputDirPath + File.separator + "log.txt");
			logFilePath = logFile.getCanonicalPath();
			FileHandler logFileHandler = new FileHandler(logFilePath);
			logFileHandler.setFormatter(new SimpleFormatter());
			logger.addHandler(logFileHandler);
		} catch (SecurityException | IOException e) {
			logger.severe("Unable to set log file: " + e.getMessage() + "!");
		}
	}
}