package heap;

import bgu.cs.util.STHierarchyRenderer;
import grammar.Node;

public class Renderer {
	//private static STGLoader templates = new STGLoader(Renderer.class, "HeapDomain");
	private static STHierarchyRenderer hrenderer = new STHierarchyRenderer(Renderer.class, "HeapDomain.stg");
	//protected static STHierarchyRenderer stringer = new STHierarchyRenderer(HeapDomain.class, "HeapDomain.stg");

	public static String render(Node n) {
		return hrenderer.render(n);
	}
	
//	public static String render(HeapDomain domain) {
//		ST template = templates.load("HeapDomain");
//		return template.render();
//		//return stringer.render(this);
//	}
}