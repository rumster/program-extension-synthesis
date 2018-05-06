package gp.separation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gp.Domain;
import grammar.CachedLanguageIterator;
import heap.BoolExpr;
import heap.PWhileInterpreter;
import heap.Stmt;
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
		extends ConditionInferencer<Store, Stmt, BoolExpr> {
	private String outputDir;
	public Map<Store, List<Boolean>> predicates;	
	
	private final CachedLanguageIterator citer;
	
	//TODO - remove @outputDir (env variables?)
	public InterpolatingConditionInferencer(Domain<Store, Stmt, BoolExpr> domain, CachedLanguageIterator citer, String outputDir){
		super(domain);
		this.outputDir = outputDir;
		this.predicates = new HashMap<>();
		this.citer = citer;	
	}
	
	@Override
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
			
			for(Store state : first) {
					thisTerm.add(predicates.get(state));
				}
			for(Store state : second) {
				othersTerm.add(predicates.get(state));
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
								if (!extendStatePredicates(state)) {
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
				test = domain.test(condition, state);
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
			List<Boolean> preds = this.predicates.get(state);
			if(preds == null) {
				preds = new ArrayList<>();
				this.predicates.put(state, preds);
			}
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
		throw new UnsupportedOperationException("unimplemented!");
	}
}
