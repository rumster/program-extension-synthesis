package jminor.codegen;

import pexyn.Semantics.Cmd;
import pexyn.Semantics.Guard;

/**
 * Renders commands and guard for a specific target language.
 * 
 * @author romanm
 */
public interface SemanticsRenderer {
	public String renderCmd(Cmd cmd);

	public String renderGuard(Guard grd);
}
