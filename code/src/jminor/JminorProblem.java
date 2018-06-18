package jminor;

import java.util.List;
import java.util.Optional;

import pexyn.Domain;
import pexyn.LoadedInterpreter;
import pexyn.SynthesisProblem;

/**
 * A synthesis problem for heap-manipulating programs.
 * 
 * @author romanm
 */
public class JminorProblem extends SynthesisProblem<Store, Stmt, BoolExpr> {
	public final JminorDomain domain;
	public final Optional<Stmt> optProg;
	public final List<Var> inputArgs;
	public final List<Var> outputArgs;
	public final List<Var> temps;

	public JminorProblem(String name, JminorDomain domain, List<Var> inputArgs, List<Var> outputArgs, List<Var> temps,
			Optional<Stmt> optProg) {
		super(name);
		this.domain = domain;
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
		result.append(domain.toString());
		return result.toString();
	}

	@Override
	public Domain<Store, Stmt, BoolExpr> domain() {
		return domain;
	}

	@Override
	public Optional<LoadedInterpreter<Store, Stmt, BoolExpr>> interpreter() {
		if (optProg.isPresent()) {
			return Optional.of(new JminorLoadedInterpreter(optProg.get()));
		} else {
			return Optional.empty();
		}
	}
}