package jminor.codegen;

import bgu.cs.util.treeGrammar.Node;
import pexyn.Semantics.Cmd;
import pexyn.Semantics.Guard;

public class JavaSemanticsRenderer implements SemanticsRenderer {
	@Override
	public String renderCmd(Cmd cmd) {
		return jminor.Renderer.render((Node) cmd);
	}

	@Override
	public String renderGuard(Guard grd) {
		return jminor.Renderer.render((Node) grd);
	}

}