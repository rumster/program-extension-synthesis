package heap.ast;

import heap.HeapProblem;

public class Main {
	public static void main(String[] args) {
		if (args.length != 1) {
			throw new IllegalArgumentException("Expected one argument, got " + args.length);
		}
		String filename = args[0];
		
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}