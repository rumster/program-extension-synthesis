package jminor.codegen;

import bgu.cs.util.STHierarchyRenderer;
import bgu.cs.util.treeGrammar.Node;
import pexyn.Semantics.Cmd;
import pexyn.Semantics.Guard;

/**
 * Returns a textual representation of statements and expressions in Dafny.
 * 
 * @author romanm
 */
public class DafnySemanticsRenderer implements SemanticsRenderer {
	private static STHierarchyRenderer hrenderer = new STHierarchyRenderer(DafnySemanticsRenderer.class, "Dafny.stg");

	@Override
	public String renderCmd(Cmd cmd) {
		return hrenderer.render((Node) cmd);
	}

	@Override
	public String renderGuard(Guard grd) {
		return hrenderer.render((Node) grd);
	}
}