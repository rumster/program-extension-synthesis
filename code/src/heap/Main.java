package heap;

import heap.ast.ASTProblem;
import heap.ast.HeapParser;
import heap.ast.ProblemCompiler;

/**
 * Synthesizes programs from a heap-formatted file.
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
		try {
			System.out.print("Parsing " + filename + "... ");
			ASTProblem root = parser.parseFile(filename);
			System.out.println("done");

			System.out.print("Compiling... ");
			ProblemCompiler compiler = new ProblemCompiler(root);
			HeapProblem problem = compiler.compile();
			System.out.println("done");
			System.out.print(problem.toString());
			return problem;
		} catch (Exception e) {
			throw new Error(e.getMessage());
		}
	}
}