package heap;

import org.stringtemplate.v4.ST;

import bgu.cs.util.STGLoader;

@Deprecated
public class Renderer {
	private static STGLoader templates = new STGLoader(Renderer.class, "HeapDomain");
	//protected static STHierarchyRenderer stringer = new STHierarchyRenderer(HeapDomain.class, "HeapDomain.stg");

	public static String render(HeapDomain domain) {
		ST template = templates.load("HeapDomain");
		return template.render();
		//return stringer.render(this);
	}
}