package grammar;

import org.stringtemplate.v4.ST;

import bgu.cs.util.STGLoader;

/**
 * Prints a human-readable representation of trees and tree grammars.
 * 
 * @author romanm
 */
public class Renderer {
	public static final Renderer v = new Renderer();
	private static STGLoader templates = new STGLoader(Renderer.class, "grammar");

	public static String render(Node op) {
		ST template = templates.load("OperatorTree");
		template.add("name", op.getName());
		for (Node n : op.getArgs()) {
			if (n instanceof Nonterminal || n instanceof Token) {
				template.add("args", n);
			} else {
				Node sub = (Node) n;
				template.add("args", render(sub));
			}
		}
		return template.render();
	}

	public static String render(Nonterminal n) {
		ST template = templates.load("Nonterminal");
		template.add("name", n.getName());
		template.add("productions", n.productions());
		return template.render();
	}

	public static String render(Grammar grammar) {
		ST template = templates.load("Grammar");
		template.add("start", grammar.start);
		for (Nonterminal n : grammar.nonterminals()) {
			template.add("nonterminals", render(n));
		}
		return template.render();
	}
}