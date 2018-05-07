package heap;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import org.stringtemplate.v4.ST;

import bgu.cs.util.FileUtils;
import bgu.cs.util.Pair;
import bgu.cs.util.STGLoader;
import bgu.cs.util.Union2;
import gp.Domain.Guard;
import gp.Domain.Update;
import gp.Example;
import gp.GPDebugger;
import gp.Plan;

/**
 * A debugger with specific utilities for heap-manipulating programs.
 * 
 * @author romanm
 */
public class HeapDebugger extends GPDebugger<Store, Stmt, BoolExpr> {
	public static String STATE_IMAGE_FILE_POSTFIX = "svg";

	public boolean logDetailedExampleRendering = false;

	protected STGLoader heapTemplates = new STGLoader(HeapDebugger.class);

	public HeapDebugger(Logger logger, String title, String outputDirPath) {
		super(logger, title, outputDirPath);
	}

	public boolean printExamples(List<Example<Store, Stmt>> examples) {
		logger.info("Visualizing examples...");
		Logger examplesLogger = logDetailedExampleRendering ? logger : null;
		ST exampleListTemplate = heapTemplates.load("examples");
		for (Example<Store, Stmt> example : examples) {
			ST indexedExampleTemplate = heapTemplates.load("indexedExample");
			for (int stepIndex = 0; stepIndex < example.size(); ++stepIndex) {
				Union2<? extends Store, ? extends Stmt> step = example.step(stepIndex);
				if (step.isT1()) {
					Store stage = step.getT1();
					String storeImageFileName = "example_" + example.id + "_" + stepIndex + "."
							+ STATE_IMAGE_FILE_POSTFIX;
					String storeImageAbsoluteFileName = outputDirPath + File.separator + storeImageFileName;
					StoreUtils.printStore(stage, storeImageAbsoluteFileName, examplesLogger);
					ST imageST = heapTemplates.load("image");
					imageST.add("name", storeImageFileName);
					indexedExampleTemplate.add("step", imageST.render());
				} else {
					Stmt stmt = step.getT2();
					ST codeST = heapTemplates.load("code");
					codeST.add("txt", Renderer.render(stmt));
					indexedExampleTemplate.add("step", codeST.render());
				}
			}
			indexedExampleTemplate.add("id", example.id);
			exampleListTemplate.add("indexedExample", indexedExampleTemplate.render());
			exampleListTemplate.add("indices", example.id);
		}
		String exampleListFileName = "examples.html";
		String exampleListPath = outputDirPath + File.separator + exampleListFileName;
		FileUtils.stringToFile(exampleListTemplate.render(), exampleListPath);
		super.printLink(exampleListFileName, "Examples");
		logger.info("Done visualizing examples.");
		return refresh();
	}

	@Override
	public void printPlan(Plan<Store, Stmt> plan, int planIndex) {
		// if (plan.isEmpty())
		// throw new UnsupportedOperationException("Missing implementation for
		// visualizing empty plans!");

		logger.info("Visualizing plan..." + planIndex);
		ST planTemplate = heapTemplates.load("plan");
		planTemplate.add("planIndex", planIndex);
		{
			Store store = plan.stateAt(0);
			String filename = "Plan" + planIndex + "_" + 0 + "." + STATE_IMAGE_FILE_POSTFIX;
			String filePath = outputDirPath + File.separator + filename;
			StoreUtils.printStore(store, filePath, logger);
			planTemplate.add("indexedStore", new Pair<String, String>(filename, "" + 0));
			planTemplate.add("indexedAction", new Pair<String, String>("initial", "" + 0));
		}
		for (int actionIndex = 0; actionIndex < plan.size() - 1; ++actionIndex) {
			Store store = plan.stateAt(actionIndex + 1);
			int storeIndex = actionIndex + 1;
			Stmt action = plan.actionAt(actionIndex);
			String filename = "Plan" + planIndex + "_" + storeIndex + "." + STATE_IMAGE_FILE_POSTFIX;
			String filePath = outputDirPath + File.separator + filename;
			StoreUtils.printStore(store, filePath, logger);
			planTemplate.add("indexedStore", new Pair<String, String>(filename, "" + storeIndex));
			String actionStr = Renderer.render(action);
			planTemplate.add("indexedAction", new Pair<String, String>(actionStr, "" + storeIndex));
		}
		String planFileName = "Plan" + planIndex + ".html";
		String planFilePath = outputDirPath + File.separator + planFileName;
		FileUtils.stringToFile(planTemplate.render(), planFilePath);
		super.printLink(planFileName, "Plan " + planIndex);
	}

	@Override
	public String renderUpdate(Update update) {
		Stmt stmt = (Stmt) update;
		return Renderer.render(stmt);
	}

	@Override
	public String renderGuard(Guard guard) {
		BoolExpr expr = (BoolExpr) guard;
		return Renderer.render(expr);
	}
}