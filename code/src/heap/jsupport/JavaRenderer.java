package heap.jsupport;

import bgu.cs.util.STHierarchyRenderer;
import grammar.Node;

/**
 * Prints a Java-formatted representation of a PWhile program.
 * 
 * @author romanm
 */
public class JavaRenderer {
	private static STHierarchyRenderer hrenderer = new STHierarchyRenderer(JavaRenderer.class, "java");

	public static String render(Node n) {
		return hrenderer.render(n);
	}
}