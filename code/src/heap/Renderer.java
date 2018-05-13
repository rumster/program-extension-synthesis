package heap;

import bgu.cs.util.STHierarchyRenderer;
import grammar.Node;

/**
 * Returns a textual representation of statements and expressions.
 * 
 * @author romanm
 */
public class Renderer {
	private static STHierarchyRenderer hrenderer = new STHierarchyRenderer(Renderer.class, "HeapDomain.stg");

	public static String render(Node n) {
		return hrenderer.render(n);
	}
}