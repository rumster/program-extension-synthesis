package gp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import bgu.cs.util.Pair;
import bgu.cs.util.rel.HashRel2;
import bgu.cs.util.rel.Rel2;

public class SynthesisProblem<StateType> {
	public String name;
	public List<Pair<StateType, StateType>> examples = new ArrayList<>();
	public Collection<Arg> tempArgs = new ArrayList<>();
	protected Rel2<ArgType, Arg> argTypeToArg = new HashRel2<>();

	public SynthesisProblem(String name, List<Pair<StateType, StateType>> examples, Collection<Arg> args,
			Collection<Arg> tempArgs) {
		this.name = name;
		for (Arg arg : args) {
			argTypeToArg.add(arg.type, arg);
		}
		for (Arg arg : tempArgs) {
			argTypeToArg.add(arg.type, arg);
			tempArgs.add(arg);
		}
	}
}