package guardInference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import bgu.cs.util.treeGrammar.CachedLanguageIterator;
import jminor.BoolExpr;
import jminor.Stmt;
import jminor.JmStore;
import pexyn.Domain;
import pexyn.Domain.Store;

/**
 * An inferencer based on finding a Boolean Craig interpolant.
 * 
 * @author romanm
 *
 * @param <StateType>
 *            The type of states over which conditions range.
 */
public class InterpolatingConditionInferencer extends ConditionInferencer<JmStore, Stmt, BoolExpr> {
	private String outputDir;
	public Map<JmStore, List<Boolean>> predicates;
	/**
	 * The domain comprised of values and predicates.
	 */
	public Domain<JmStore, Stmt, BoolExpr> domain;	

	private final CachedLanguageIterator citer;

	// TODO - remove @outputDir (env variables?)
	public InterpolatingConditionInferencer(Domain<JmStore, Stmt, BoolExpr> domain, CachedLanguageIterator citer,
			String outputDir) {
		this.domain = domain;
		this.outputDir = outputDir;
		this.predicates = new HashMap<>();
		this.citer = citer;
	}

	@Override
	public Optional<BoolExpr> infer(Collection<? extends Store> first, Collection<? extends Store> second) {
		BoolExpr result = null;
		Interpolator intp = new Interpolator(outputDir);

		int maxPredicateIndx = 0;
		for (Collection<? extends Store> states : Arrays.asList(first, second)) {
			initStatePredicates(states);
			for (var v : states) {
				JmStore state = (JmStore) v;
				if (maxPredicateIndx < this.predicates.get(state).size()) {
					maxPredicateIndx = this.predicates.get(state).size();
				}
			}
		}

		while (true) {
			List<List<Boolean>> thisTerm = new ArrayList<>();
			List<List<Boolean>> othersTerm = new ArrayList<>();

			for (var v : first) {
				JmStore state = (JmStore) v;
				thisTerm.add(predicates.get(state));
			}
			for (var v : second) {
				JmStore state = (JmStore) v;
				othersTerm.add(predicates.get(state));
			}

			BoolExpr interpol = intp.genInterpolant(thisTerm, othersTerm, citer);
			if (interpol == null) {
				// 1) first evaluate EXISTING predicates for all the states
				// (make sure the predicates lists cover all the existing predicates for each
				// state)
				// 2) when the lists have the same length - generate a NEW predicate
				boolean updateall = true;
				while (updateall) {
					for (Collection<? extends Store> states : Arrays.asList(first, second)) {
						for (var v : states) {
							JmStore state = (JmStore) v;
							int nextPredIndx = this.predicates.get(state).size();
							if (nextPredIndx < maxPredicateIndx) {
								updateall = false;
								if (!extendStatePredicates(state)) {
									// no more predicates are available
									return Optional.empty();
								}
							}
						}
					}

					if (updateall) {
						maxPredicateIndx++;
					}
				}
			} else {
				result = interpol;
				break;
			}
		}
		return Optional.of(result);
	}

	private boolean extendStatePredicates(JmStore state) {
		List<Boolean> statePredicates = this.predicates.get(state);
		int nextPredIndx = statePredicates.size();
		Boolean test = null;
		while (test == null) {
			if (citer.has(nextPredIndx)) {
				// TODO: unsafe casting to BoolExpr - refactor
				BoolExpr condition = (BoolExpr) citer.get(nextPredIndx);
				test = domain.test(condition, state);
				statePredicates.add(test);
				nextPredIndx++;
			} else {
				break;
			}
		}
		return (test != null);
	}

	private void initStatePredicates(Collection<? extends Store> states) {
		for (var v : states) {
			JmStore state = (JmStore) v;
			Boolean initialized = false;
			List<Boolean> preds = this.predicates.get(state);
			if (preds == null) {
				preds = new ArrayList<>();
				this.predicates.put(state, preds);
			}
			for (Boolean pred : this.predicates.get(state)) {
				if (pred != null) {
					initialized = true;
					break;
				}
			}
			if (!initialized) {
				extendStatePredicates(state);
			}
		}
	}

	@Override
	public List<BoolExpr> guards() {
		return List.of();
	}
}
