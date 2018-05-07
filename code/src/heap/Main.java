package heap;

import heap.ast.ASTProblem;
import heap.ast.HeapParser;
import heap.ast.ProblemCompiler;

/**
 * Synthesizes programs from a heap-format specification file.
 * 
 * @author romanm
 */
public class Main extends HeapRunner {
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

	@Override
	public HeapProblem genProblem() {
		HeapParser parser = new HeapParser();
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
		HeapProblem problem = compiler.compile();
		System.out.println("done");
		return problem;
	}
}