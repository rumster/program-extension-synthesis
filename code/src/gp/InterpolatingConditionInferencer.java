package gp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gp.separation.ConditionInferencer;
import grammar.CachedLanguageIterator;
import grammar.CostBadConditions;
import grammar.CostFun;
import grammar.CostSize;
import grammar.CostSum;
import heap.BoolExpr;
import heap.PWhileGrammarGen;
import heap.PWhileInterpreter;
import heap.Store;

/**
 * An inferencer based on finding a Boolean Craig interpolant.
 * 
 * @author romanm
 *
 * @param <StateType>
 *            The type of states over which conditions range.
 */
public class InterpolatingConditionInferencer
		extends ConditionInferencer<Store, BoolExpr> {
	private String outputDir;
	public Map<Store, List<Boolean>> predicates;	
	
	private final CachedLanguageIterator citer;
	public static CostFun cost = new CostSum(new CostBadConditions(), new CostSize());
	//limit dereference depth to 0 levels (x == y)
	private float threshold = 4;
	
	public InterpolatingConditionInferencer(String outputDir){
		this.outputDir = outputDir;
		this.predicates = new HashMap<>();
		
		citer = new CachedLanguageIterator(PWhileGrammarGen.neq_arithm, cost, threshold);
	}
	
	@Override
	//public List<ConditionType> inferSeparators(List<Collection<StateType>> labelToStates){
	public BoolExpr inferSeparator(Collection<Store> first, Collection<Store> second) {
		BoolExpr result = null;
		Interpolator intp = new Interpolator(outputDir);		
		
		int maxPredicateIndx = 0;
		for(Collection<Store> states : Arrays.asList(first, second)) {
			initStatePredicates(states);			
			for(Store state : states) {
				if(maxPredicateIndx < this.predicates.get(state).size()){
					maxPredicateIndx = this.predicates.get(state).size();
				}
			}
		}
		
		while(true){
			List<List<Boolean>> thisTerm = new ArrayList<>();			
			List<List<Boolean>> othersTerm = new ArrayList<>();
			
			for(Collection<Store> states : Arrays.asList(first, second)) {
				for(Store state : states) {
					thisTerm.add(predicates.get(state));
				}
			}

			BoolExpr interpol = intp.genInterpolant(thisTerm, othersTerm, citer);
			if(interpol == null){
				// 1) first evaluate EXISTING predicates for all the states
				//    (make sure the predicates lists cover all the existing predicates for each state)
				// 2) when the lists have the same length - generate a NEW predicate
				boolean updateall = true;
				while(updateall) {
					for(Collection<Store> states : Arrays.asList(first, second)) {
						for(Store state : states) {
							int nextPredIndx = this.predicates.get(state).size();
							if(nextPredIndx < maxPredicateIndx){
								updateall = false;							
								if (extendStatePredicates(state)) {
									//no more predicates are available
									return null;
								}
							}
						}
					}					
					
					if(updateall){
						maxPredicateIndx++;
					}
				}
			}
			else{
				result = interpol;
				break;
			}
		}
		return result;
	}
	
	
	private boolean extendStatePredicates(Store state){
		List<Boolean> statePredicates = this.predicates.get(state);
		int nextPredIndx = statePredicates.size();
		Boolean test = null;
		while(test == null) {
			if (citer.has(nextPredIndx)) {
				//TODO: unsafe casting to BoolExpr - refactor
				BoolExpr condition = (BoolExpr)citer.get(nextPredIndx);
				test = PWhileInterpreter.v.test(condition, state);
				//null, if predicate value is undefined for this state
				statePredicates.add(test);
				nextPredIndx++;
			}
			else {
				break;
			}
		}
		return (test != null);
	}
	
	
	private void initStatePredicates(Collection<Store> states){		
		for (Store state : states) {
			Boolean initialized = false;
			for(Boolean pred : this.predicates.get(state)) {
				if(pred != null) {
					initialized = true;
					break;
				}
			}
			if(!initialized) {
				extendStatePredicates(state);
			}
		}
	}

	@Override
	public List<BoolExpr> inferSeparators(List<Collection<Store>> labelToStates) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	/*
	public boolean inferConditionsForNodeSMT(CFG cfg, CFGNode node) {	
		
		
		Interpolator intp = new Interpolator();
		HashRel2<Operator, State> operatorToStates = new HashRel2<>();
		int maxPredicateIndx = 0;
		
		initStatePredicates(node);
		for (State state : node.states) {
			for (MultiGraph.Edge<State, Label<Operator>> outTransition : cfg.transitions.succEdges(state)) {
				CFG.Label<Operator> l = outTransition.getLabel();
				if(node.getLables().contains(l)){
					Operator op = l.getValue();
					operatorToStates.add(op, state);
				}
			}			
			if(maxPredicateIndx < state.predicates.size()){
				maxPredicateIndx = state.predicates.size();
			}
		}
	
		// Find a condition for each outgoing edge.		
		for (MultiGraph.Edge<CFGNode, Action> edge : cfg.succEdges(node)) {
			Action action = edge.getLabel();
			while(true){
				List<List<Boolean>> thisTerm = new ArrayList<>();			
				List<List<Boolean>> othersTerm = new ArrayList<>();
				for (MultiGraph.Edge<CFGNode, Action> otherEdge : cfg.succEdges(node)) {
					Action otherAction = otherEdge.getLabel();
					Collection<State> otherEdgeStates = operatorToStates.select1(otherAction.update);

					List<List<Boolean>> cur;
					if(edge != otherEdge){
						cur = othersTerm;
					}
					else{
						cur = thisTerm;
					}
					for(State s : otherEdgeStates){
						cur.add(s.predicates);					
					}					
				}	

				Operator interpol = intp.genInterpolant(thisTerm, othersTerm, citer);
				if(interpol == null){
					// 1) first evaluate EXISTING predicates for all the states
					//    (make sure the predicates lists cover all the existing predicates for each state)
					// 2) when the lists have the same length - generate a NEW predicate
					boolean updateall = true;
					for(State state : node.states){
						int nextPredIndx = state.predicates.size();
						if(nextPredIndx < maxPredicateIndx){
							updateall = false;							
							if (citer.has(nextPredIndx)) {
								Operator condition = citer.get(nextPredIndx);
								Boolean test = Interpreter.v.test(condition, state);
								//null, if predicate value is undefined for this state
								state.predicates.add(test);
							} else {
								return false;
							}
						}
					}
					if(updateall){
						maxPredicateIndx++;
					}
				}
				else{
					action.guard = interpol;
					StringBuilder debugStr = new StringBuilder();
					debugStr.append("Found separator: " + interpol + "\n");
					debugStr.append("-------------------------------------");
					Globals.printer().printStepCodeFile("separators" + usageCounter + ".txt", debugStr.toString(),
							"debug information for separators computation");
					//usageCounter++;
					break;
				}
			}
		}		
		
		
		return true;
	}
	*/
	
	
	
}
