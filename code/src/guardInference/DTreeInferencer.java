package guardInference;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import bgu.cs.util.rel.HashRel2;
import bgu.cs.util.rel.Rel2;
import pexyn.Domain;
import pexyn.Plan;
import pexyn.Domain.Guard;
import pexyn.Domain.Cmd;
import pexyn.Domain.Store;

/**
 * A greedy procedure for learning a decision tree.
 * 
 * @author romanm
 *
 * @param <ExampleType>
 *            The type of examples to classify.
 * @param <LabelType>
 *            The type of class labels.
 * @param <FeatureType>
 *            The type of (Boolean) features.
 * 
 * @TODO Make this class generic: separate from Cmd and other domain related
 *       type parameters.
 */
public class DTreeInferencer<ExampleType extends Store, LabelType extends Cmd, FeatureType extends Guard>
		extends ConditionInferencer<ExampleType, LabelType, FeatureType> {
	/**
	 * The set of Boolean attributes used to compute the classifier.
	 */
	protected final List<FeatureType> propositions;

	protected final Domain<ExampleType, LabelType, FeatureType> domain;

	public DTreeInferencer(Domain<ExampleType, LabelType, FeatureType> domain,
			List<Plan<ExampleType, LabelType>> plans) {
		this.propositions = domain.generateBasicGuards(plans);
		this.domain = domain;
	}

	@Override
	public List<Optional<FeatureType>> inferList(List<Collection<? extends Store>> classes) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<FeatureType> infer(Collection<? extends Store> first, Collection<? extends Store> second) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<Map<Cmd, ? extends Guard>> infer(Rel2<Cmd, Store> updateToValue) {
		Node root = new Node(updateToValue);
		var foundTree = refineNode(root);
		if (foundTree) {
			var result = compileGuards(root, updateToValue.all1());
			return Optional.of(result);
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Generates a Boolean expression for each update from the tree.
	 * 
	 * @param root
	 *            A decision tree.
	 * @param updates
	 *            The set of updates, which serve as the labels for the classifier.
	 * @return A mapping from an update to the respective guard.
	 */
	protected Map<Cmd, ? extends Guard> compileGuards(Node root, Collection<Cmd> updates) {
		var result = new HashMap<Cmd, FeatureType>();
		for (var update : updates) {
			FeatureType guard = compileGuard(root, update);
			result.put(update, guard);
		}
		return result;
	}

	/**
	 * Generates the Boolean expression for the given update at the given node.
	 */
	protected FeatureType compileGuard(Node node, Cmd update) {
		FeatureType result;
		if (node.pure()) {
			if (node.pureLabel() == update) {
				return domain.getTrue();
			} else {
				return null;
			}
		} else {
			FeatureType posGuard = compileGuard(node.pos, update);
			FeatureType negGuard = compileGuard(node.neg, update);
			if (posGuard == null && negGuard != null) {
				result = simplifiedAnd(domain.not(node.splitter), negGuard);
			} else if (negGuard == null && posGuard != null) {
				result = simplifiedAnd(node.splitter, posGuard);
			} else if (posGuard == null && negGuard == null) {
				result = null;
			} else {
				result = domain.or(simplifiedAnd(node.splitter, posGuard),
						simplifiedAnd(domain.not(node.splitter), negGuard));
			}
		}

		return result;
	}

	/**
	 * A conjunction with simplification for trivial formulas.
	 */
	protected FeatureType simplifiedAnd(FeatureType first, FeatureType second) {
		if (first == domain.getTrue()) {
			return second;
		} else if (second == domain.getTrue()) {
			return first;
		} else {
			return domain.and(first, second);
		}
	}

	/**
	 * Computes the classifier at the given node.
	 */
	protected boolean refineNode(Node root) {
		if (root.pure()) {
			return true;
		} else {
			var update = root.updateToValue.all1().iterator().next();
			var optSplitter = findBestSplitter(root.updateToValue, update);
			if (!optSplitter.isPresent()) {
				return false;
			}
			split(root, optSplitter.get());
			var subTreeSuccessfullyBuilt = refineNode(root.pos);
			if (!subTreeSuccessfullyBuilt) {
				return false;
			}
			subTreeSuccessfullyBuilt = refineNode(root.neg);
			if (!subTreeSuccessfullyBuilt) {
				return false;
			}
			return true;
		}
	}

	@Override
	public List<FeatureType> guards() {
		return propositions;
	}

	protected Optional<FeatureType> findBestSplitter(Rel2<Cmd, Store> updateToValue, Cmd updateToSplit) {
		// double numOfValues = updateToValue.size();
		var bestScore = 0d;
		FeatureType bestGuard = null;
		for (FeatureType guard : propositions) {
			// var gini = 0d;
			// int numOfSatisfiedValues = 0;
			// for (var val : updateToValue.all2()) {
			// @SuppressWarnings("unchecked")
			// boolean holds = domain.test(guard, (StoreType) val);
			// if (holds) {
			// ++numOfSatisfiedValues;
			// }
			// }
			// var score = numOfSatisfiedValues;
			var numUpdateToSplitPairs = 0d;
			var numNonUpdateToSplitPairs = 0d;
			var numPosUpdateToSplitPairs = 0d;
			var numPosNonUpdateToSplitPairs = 0d;
			for (var pair : updateToValue.all()) {
				@SuppressWarnings("unchecked")
				ExampleType val = (ExampleType) pair.second;
				boolean holds = domain.test(guard, val);
				if (pair.first == updateToSplit) {
					++numUpdateToSplitPairs;
					if (holds) {
						++numPosUpdateToSplitPairs;
					}
				} else {
					++numNonUpdateToSplitPairs;
					if (holds) {
						++numPosNonUpdateToSplitPairs;
					}
				}
			}
			var updateToSplitRatio = numPosUpdateToSplitPairs / numUpdateToSplitPairs;
			var nonUpdateToSplitRatio = numPosNonUpdateToSplitPairs / numNonUpdateToSplitPairs;
			var score = updateToSplitRatio * (1 - nonUpdateToSplitRatio);
			// var p = numOfSatisfiedValues / numOfValues;
			// gini = p * (1 - p);
			if (score > bestScore) {
				bestScore = score;
				bestGuard = guard;
			}
		}

		if (bestScore > 0) {
			return Optional.ofNullable(bestGuard);
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Populates the sub-nodes of the given node with the given splitter.
	 */
	protected void split(Node node, FeatureType splitter) {
		var posVals = new HashRel2<Cmd, Store>();
		var negVals = new HashRel2<Cmd, Store>();
		var posNode = new Node(posVals);
		var negNode = new Node(negVals);
		node.splitter = splitter;
		node.pos = posNode;
		node.neg = negNode;
		for (var updateValue : node.updateToValue.all()) {
			var update = updateValue.first;
			var value = updateValue.second;
			@SuppressWarnings("unchecked")
			boolean holds = domain.test(splitter, (ExampleType) value);
			if (holds) {
				posVals.add(update, value);
			} else {
				negVals.add(update, value);
			}
		}
	}

	/**
	 * A decision tree node.
	 * 
	 * @author romanm
	 */
	public class Node {
		/**
		 * The subset of labeled values that need to be classified at this node.
		 */
		Rel2<Cmd, Store> updateToValue;

		/**
		 * The basic proposition used to split the values at this node into the
		 * sub-nodes.
		 */
		public FeatureType splitter;

		/**
		 * The node into which the subset of value that satisfy the splitter are sent.
		 */
		public Node pos;

		/**
		 * The node into which the subset of value that do not satisfy the splitter are
		 * sent.
		 */
		public Node neg;

		public Node(Rel2<Cmd, Store> updateToValue) {
			this.updateToValue = updateToValue;
		}

		public boolean leaf() {
			return splitter == null;
		}

		public boolean pure() {
			assert updateToValue.size() > 0;
			int uniqueKeys = new HashSet<Cmd>(updateToValue.all1()).size();
			return uniqueKeys == 1;
		}

		public Cmd pureLabel() {
			assert pure();
			var result = updateToValue.all1().iterator().next();
			return result;
		}
	}
}
