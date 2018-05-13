package heap;

import java.util.Optional;

import gp.Domain;
import gp.LoadedInterpreter;
import gp.SynthesisProblem;

/**
 * A synthesis problem for heap-manipulating programs.
 * 
 * @author romanm
 */
public class HeapProblem extends SynthesisProblem<Store, Stmt, BoolExpr> {
	public final HeapDomain domain;
	public final Optional<Stmt> optProg;

	public HeapProblem(String name, HeapDomain domain, Optional<Stmt> optProg) {
		super(name);
		this.domain = domain;
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
			return Optional.of(new HeapLoadedInterpreter(optProg.get()));
		} else {
			return Optional.empty();
		}
	}
}