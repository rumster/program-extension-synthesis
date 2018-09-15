package pexyn.grammarInference;

import java.util.Optional;

import jminor.BoolExpr;
import jminor.JmStore;
import jminor.Stmt;
import pexyn.ArrayListTrace;
import pexyn.Semantics;
import pexyn.Trace;

public class CFGInterpreter {
	private final Grammar cfg; 
	private final Semantics<JmStore, Stmt, BoolExpr> semantics;
	private Trace<JmStore, Stmt> trace;
	private JmStore currState;
	int maxSteps;
	public CFGInterpreter(Grammar cfg, Semantics<JmStore, Stmt, BoolExpr> semantics) {
		this.cfg = cfg;
		this.semantics = semantics;
		
	}

	private Optional<JmStore> run(Terminal sym){
		var stmtLtr = (StmtLetter)sym.id;
		var optNextVal = semantics.apply(stmtLtr.cmd, currState);
		if (optNextVal.isPresent()) {
			currState = optNextVal.get();
			if (trace != null) {
				trace.append(stmtLtr.cmd, new JmStore(currState));
			}
			if (--maxSteps == 0) {
				return Optional.empty();
			}
		} else {
			return Optional.empty();
		}
		return Optional.of(currState);
	}
	
	private Optional<JmStore> run(Nonterminal sym){
		assert (sym.getClass() == Nonterminal.class);
		Nonterminal nt = (Nonterminal) sym;
		var prods = nt.getProductions();
		var guards = nt.getGuards();
		for(int i=0; i<prods.size(); i++) {
			if (guards.size() <= i || semantics.test((BoolExpr)guards.get(i), currState)) {
				return run(prods.get(i));
			}
		}
		//no fitting condition found.
		return Optional.empty();
	}
	
	private Optional<JmStore> run(Symbol sym){
		assert(false);
		return Optional.empty();
	}
	
	private Optional<JmStore> run(SententialForm Prod) {
		for (int i=0; i< Prod.size(); i++) {
			Symbol sym = Prod.get(i);
			Optional<JmStore> optNextVal = null;
			if(sym.getClass() == Nonterminal.class) {
				optNextVal = run((Nonterminal)sym);
			} else {
				optNextVal = run((Terminal)sym);
			}
			if (!optNextVal.isPresent()) {
				return Optional.empty();
			}
		}
		return Optional.of(currState);
	}
	
	public Optional<Trace<JmStore, Stmt>>  genTrace(JmStore input, int maxSteps) {
		this.maxSteps = maxSteps;
		this.currState = input;
		trace = new ArrayListTrace<>(input);
		var optVal = run(cfg.getCurrStartProduct());
		if (optVal.isPresent()) {
			var result = trace;
			trace = null; // Release unneeded memory resources.
			return Optional.of(result);
		} else {
			return Optional.empty();
		}
	}

}
