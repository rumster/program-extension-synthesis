package heap;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import org.stringtemplate.v4.ST;

import bgu.cs.util.FileUtils;
import bgu.cs.util.Pair;
import bgu.cs.util.STGLoader;
import gp.GPDebugger;
import gp.InputOutputExample;
import gp.Plan;

/**
 * A debugger with specific utilities for heap-manipulating programs.
 * 
 * @author romanm
 */
public class HeapDebugger extends GPDebugger<Store, BasicStmt, Condition> {
	public static String STATE_IMAGE_FILE_POSTFIX = "svg";

	public boolean logDetailedExampleRendering = false;

	protected STGLoader heapTemplates = new STGLoader(HeapDebugger.class);

	public HeapDebugger(Logger logger, String title, String outputDirPath) {
		super(logger, title, outputDirPath);
	}

	public boolean printExamples(List<InputOutputExample<Store>> examples) {
		logger.info("Visualizing examples...");
		Logger examplesLogger = logDetailedExampleRendering ? logger : null;
		ST exampleListTemplate = heapTemplates.load("examples");
		for (InputOutputExample<Store> example : examples) {
			String inputFileName = outputDirPath + File.separator + "example_input_" + example.index + "."
					+ STATE_IMAGE_FILE_POSTFIX;
			String outputFileName = outputDirPath + File.separator + "example_output_" + example.index + "."
					+ STATE_IMAGE_FILE_POSTFIX;
			StoreUtils.printStore(example.first, inputFileName, examplesLogger);
			StoreUtils.printStore(example.second, outputFileName, examplesLogger);

			ST indexedExampleTemplate = heapTemplates.load("indexedExample");
			indexedExampleTemplate.add("inputImageFileName", inputFileName);
			indexedExampleTemplate.add("outputImageFileName", outputFileName);
			indexedExampleTemplate.add("index", example.index);
			exampleListTemplate.add("indices", example.index);
			exampleListTemplate.add("indexedExample", indexedExampleTemplate.render());
		}
		String exampleListFileName = outputDirPath + File.separator + "examples.html";
		FileUtils.stringToFile(exampleListTemplate.render(), exampleListFileName);
		super.printLink(exampleListFileName, "Examples");
		logger.info("Done visualizing examples.");
		return refresh();
	}

	@Override
	public void printPlan(Plan<Store, BasicStmt> plan, int planIndex) {
		if (plan.isEmpty())
			throw new UnsupportedOperationException("Missing implementation for visualizing empty plans!");

		logger.info("Visualizing plan..." + planIndex);
		ST planTemplate = heapTemplates.load("plan");
		planTemplate.add("planIndex", planIndex);
		{
			Store store = plan.stateAt(0);
			String filename = outputDirPath + File.separator + "Plan" + planIndex + "_" + 0 + "."
					+ STATE_IMAGE_FILE_POSTFIX;
			StoreUtils.printStore(store, filename, logger);
			planTemplate.add("indexedStore", new Pair<String, String>(filename, "" + 0));
			planTemplate.add("indexedAction", new Pair<String, String>("initial", "" + 0));
		}
		for (int actionIndex = 0; actionIndex < plan.size() - 1; ++actionIndex) {
			Store store = plan.stateAt(actionIndex + 1);
			int storeIndex = actionIndex + 1;
			BasicStmt action = plan.actionAt(actionIndex);
			String filename = outputDirPath + File.separator + "Plan" + planIndex + "_" + storeIndex + "."
					+ STATE_IMAGE_FILE_POSTFIX;
			StoreUtils.printStore(store, filename, logger);
			planTemplate.add("indexedStore", new Pair<String, String>(filename, "" + storeIndex));
			planTemplate.add("indexedAction", new Pair<String, String>(action.toString(), "" + storeIndex));
		}
		String planFileName = outputDirPath + File.separator + "Plan" + planIndex + ".html";
		FileUtils.stringToFile(planTemplate.render(), planFileName);
		super.printLink(planFileName, "Plan " + planIndex);
	}
}