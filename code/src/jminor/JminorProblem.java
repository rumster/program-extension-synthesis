package jminor;

import java.util.List;
import java.util.Optional;

import pexyn.Semantics;
import pexyn.LoadedInterpreter;
import pexyn.SynthesisProblem;

/**
 * A synthesis problem for Jminor programs.
 * 
 * @author romanm
 */
public class JminorProblem extends SynthesisProblem<JmStore, Stmt, BoolExpr> {
	public final JminorSemantics semantics;
	public final Optional<Stmt> optProg;
	public final List<Var> inputArgs;
	public final List<Var> outputArgs;
	public final List<Var> temps;

	public JminorProblem(String name, JminorSemantics semantics, List<Var> inputArgs, List<Var> outputArgs,
			List<Var> temps, Optional<Stmt> optProg) {
		super(name);
		this.semantics = semantics;
		this.inputArgs = inputArgs;
		this.outputArgs = outputArgs;
		this.temps = temps;
		this.optProg = optProg;
	}

	@Override
	public String toString() {
		var result = new StringBuilder();
		if (optProg.isPresent()) {
			var prog = optProg.get();
			result.append(Renderer.render(prog));
			result.append(System.lineSeparator());
		}
		result.append(semantics.toString());
		return result.toString();
	}

	@Override
	public Semantics<JmStore, Stmt, BoolExpr> semantics() {
		return semantics;
	}

	@Override
	public Optional<LoadedInterpreter<JmStore, Stmt, BoolExpr>> interpreter() {
		if (optProg.isPresent()) {
			return Optional.of(new JminorLoadedInterpreter(optProg.get()));
		} else {
			return Optional.empty();
		}
	}
}