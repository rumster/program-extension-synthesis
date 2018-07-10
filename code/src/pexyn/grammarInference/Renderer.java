package pexyn.grammarInference;

import org.stringtemplate.v4.ST;

import bgu.cs.util.STGLoader;

/**
 * Prints a human-readable representation of objects.
 * 
 * @author romanm
 */
public class Renderer {
	public static final Renderer v = new Renderer();
	private static STGLoader templates = new STGLoader(Renderer.class, "grammar");

	public static String render(SententialForm s) {
		ST template = templates.load("SententialForm");
		template.add("productions", s);
		return template.render();

	}

	public static String render(Nonterminal n) {
		ST template = templates.load("Nonterminal");
		template.add("name", n.getName());
		for (SententialForm prod : n.getProductions()) {
			template.add("productions", render(prod));
		}
		return template.render();
	}

	public static String render(Grammar grammar) {
		ST template = templates.load("Grammar");
		template.add("nonterminals", render(grammar.getStart()));
		for (Nonterminal n : grammar.getNonterminals()) {
			template.add("nonterminals", render(n));
		}
		return template.render();
	}
}