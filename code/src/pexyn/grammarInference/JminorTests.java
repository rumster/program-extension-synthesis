package pexyn.grammarInference;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import pexyn.Example;
import pexyn.PETISynthesizer;
import pexyn.Trace;
import pexyn.planning.AStar;


class OperatorString extends Letter {
	final String str;

	public OperatorString(String s) {
		super();
		this.str = s;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return str;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OperatorString other = (OperatorString) obj;
		return (str.equals(other.str));
	}



}
class JminorTrace extends ArrayList<OperatorString>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}

/**
 * Synthesizes programs from a heap-format specification file.
 * 
 * @author romanm
 */
public class JminorTests {
		protected final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	private static final String PROPERTIES_FILE_NAME = "pexyn.properties";

	private String outputDirPath = null;

	private Timer inferrenceTime = new Timer();
	private JminorDebugger debugger = null;

	private Configuration config = null;

	private String filename;

	public static void main(String[] args) {
		JminorTests main = new JminorTests();
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
		logger.setLevel(Level.OFF);
		var configs = new Configurations();
		try {
			config = configs.properties(new File(PROPERTIES_FILE_NAME));
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		var allFiles = Arrays.asList("bst_find.spec", "factorial.spec", "fibonacci.spec",
				"gcd.spec", "sll_bubble_sort.spec", "sll_fill.spec", "sll_find.spec",
				"sll_find_cycle.spec", "sll_max.spec","sll_reverse.spec",
				"sll_reverse_merge.spec", "sqrt_fast.spec", "sqrt_slow.spec",
				 "zune_bug.spec");/**/
		
		//these work perfect, should include them once in a while to check regression:
		var otherFiles = Arrays.asList("factorial.spec", "fibonacci.spec",
				"sll_fill.spec",
				"sll_find_cycle.spec", "sll_max.spec","sll_reverse.spec",
				"sqrt_slow.spec");
		
		var files1 = Arrays.asList("bst_find.spec",
				"gcd.spec", "sll_bubble_sort.spec", "sll_find.spec",
				"sll_reverse_merge.spec", "sqrt_fast.spec", 
				 "zune_bug.spec");/**/
		var files22 = Arrays.asList("zune_bug.spec");
		var files12 = Arrays.asList("gcd.spec");
		var files = Arrays.asList("sll_find.spec");
		for(String file: allFiles) {
			this.filename = file;
			debugger = new JminorDebugger(config, logger, filename, outputDirPath);
			logger.info("Synthesizer: started");
			inferrenceTime.reset();
			//planningTime.reset();
			try {
				JminorProblem problem = genProblem();
				var planner = new AStar<JmStore, Stmt>(new BasicJminorTR(problem.semantics));
				var synthesizer = new PETISynthesizer<JmStore, Stmt, BoolExpr>(planner, config, debugger);
				var plans = synthesizer.genPlans(problem);
				inferrenceTime.start();
				testConvergence(plans);

			} catch (Throwable t) {
				debugger.severe(t.toString());
				t.printStackTrace();
			} finally {
				inferrenceTime.stop();
				//logger.info("Planning time: " + planningTime.toSeconds());
				logger.info("Synthesizer: done! (" + inferrenceTime.toSeconds() + ")");
			}
		}
	}
	private static void testConvergence(Map<Example<JmStore, Stmt>, Trace<JmStore, Stmt>> map) {
		Sequential x = new Sequential();
		Grammar stableGrammar = new Grammar();
		String stableExample = "None";
		
		for(Entry<Example<JmStore, Stmt>, Trace<JmStore, Stmt>> example: map.entrySet()) {
			JminorTrace actionTrace = new JminorTrace();
			for(Stmt step: example.getValue().actions()) {
				actionTrace.add(new OperatorString(step.toString()));
			}
 			Grammar currGrammar = x.addExample(actionTrace);
			if (!currGrammar.equals(stableGrammar)) {
				stableGrammar = new Grammar(currGrammar);
				stableExample = example.getKey().toString();
			}
		}
		System.out.println("Converging trace with " + stableExample + " out of " + map.size() +" :");
		//System.out.println(traceGen.apply(stableLen));
		System.out.print("Final grammar:");
		System.out.println(stableGrammar.toString());
		System.out.println("-------------------------------------------------------------\n\n");
	}
}