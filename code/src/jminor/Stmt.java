package jminor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import bgu.cs.util.treeGrammar.Node;
import pexyn.Semantics.Cmd;

/**
 * A base class for Jminor statements.
 * 
 * @author romanm
 */
public abstract class Stmt extends Node implements Cmd {
	protected final List<Node> args;

	@Override
	public final List<Node> getArgs() {
		return args;
	}

	/**
	 * Tests whether this statement can be applied to the given store.
	 */
	public boolean enabled(JmStore store) {
		JmStore result = JminorInterpreter.v.run(this, store, JminorInterpreter.v.guessMaxSteps(this, store)).get();
		return !(result instanceof JmStore.JmErrorStore);
	}

	@Override
	public String toString() {
		return Renderer.render(this);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o == this) {
			return true;
		}
		if (!this.getClass().equals(o.getClass())) {
			return false;
		}
		Stmt other = (Stmt) o;
		for (var i = 0; i < args.size(); ++i) {
			if (!args.get(i).equals(other.args.get(i))) {
				return false;
			}
		}
		return true;
	}

	protected Stmt(Collection<Node> nodes) {
		super(countNonterminals(nodes));
		this.args = Collections.unmodifiableList(new ArrayList<>(nodes));
	}

	protected Stmt(Node... args) {
		super(countNonterminals(args));
		var argList = new ArrayList<Node>(args.length);
		for (Node n : args) {
			argList.add(n);
		}
		this.args = Collections.unmodifiableList(argList);
	}

	protected Stmt() {
		super(0);
		this.args = Collections.emptyList();
	}

	protected Stmt(int numOfNonterminals) {
		super(numOfNonterminals);
		this.args = Collections.emptyList();
	}
}