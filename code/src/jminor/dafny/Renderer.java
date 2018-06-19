package jminor.dafny;

import bgu.cs.util.STHierarchyRenderer;
import bgu.cs.util.treeGrammar.Node;

/**
 * Returns a textual representation of statements and expressions in Dafny.
 * 
 * @author romanm
 */
public class Renderer {
	private static STHierarchyRenderer hrenderer = new STHierarchyRenderer(Renderer.class, "Dafny.stg");

	public static String render(Node n) {
		return hrenderer.render(n);
	}
}