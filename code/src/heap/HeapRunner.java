package heap;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import bgu.cs.util.Timer;
import gp.AStar;
import gp.NaiveSynthesizer;
import gp.Planner;
import gp.controlFlowGraph.CFG;
import gp.controlFlowGraph.CFGGeneralizer;
import gp.controlFlowGraph.RPNIGeneralizer;

/**
 * Heap-manipulating program synthesis application.
 * 
 * @author romanm
 */
public abstract class HeapRunner {
	protected final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	private static final String OUTPUT_DIR_KEY = "outputDir";
	private static final String PROPERTIES_FILE_NAME = "gp.properties";

	private String outputDirPath = null;

	private Timer synthesisTime = new Timer();
	private Timer planningTime = new Timer();

	private File logFile = null;
	private String logFilePath = null;

	private HeapDebugger debugger = null;

	private Configuration config = null;

	protected abstract HeapProblem genProblem();

	/**
	 * Starts the ball rolling.
	 */
	public void run() {
		Configurations configs = new Configurations();
		try {
			config = configs.properties(new File(PROPERTIES_FILE_NAME));
			outputDirPath = config.getString(OUTPUT_DIR_KEY, "./");
			File dir = new File(outputDirPath);
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
			HeapProblem problem = genProblem();
			debugger = new HeapDebugger(logger, problem.name, outputDirPath);
			debugger.printLink(logFilePath, "Events log");
			debugger.printCodeFile("problem.txt", problem.toString(), "Specification");
			debugger.printExamples(problem.examples);
			synthesisTime.start();
			Planner<Store, Stmt> planner = new AStar<Store, Stmt>(new BasicHeapTR(problem.domain));
			CFGGeneralizer<Store, Stmt, BoolExpr> generalizer = new RPNIGeneralizer(debugger, outputDirPath);
			NaiveSynthesizer<Store, Stmt, BoolExpr> synthesizer = new NaiveSynthesizer<>(planner, generalizer, logger,
					debugger);
			CFG<Store, Stmt, BoolExpr> result = new CFG<>();
			boolean ok = synthesizer.synthesize(problem, result);
			if (!ok) {
				logger.info("fail!");
			} else {
				logger.info("success!");
				debugger.printCodeFile("synth-code.txt", result.toString(), "Synthesis result");
				logger.info("Verifying synthesized program...");
				boolean resultCorrect = problem.test(result);
				logger.info(resultCorrect ? "ok" : "erroneous");
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
		String outputDirProp = config.getString("outputDir", "output");
		File outputDirFile = new File(outputDirProp);
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
			logFile = new File(outputDirPath + "/log.txt");
			logFilePath = logFile.getCanonicalPath();
			FileHandler logFileHandler = new FileHandler(logFilePath);
			logFileHandler.setFormatter(new SimpleFormatter());
			logger.addHandler(logFileHandler);
		} catch (SecurityException | IOException e) {
			logger.severe("Unable to set log file: " + e.getMessage() + "!");
		}
	}
}