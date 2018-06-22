package pexyn.guardInference;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import bgu.cs.util.rel.HashRel2;
import bgu.cs.util.rel.Rel2;
import pexyn.Semantics;
import pexyn.Semantics.Cmd;
import pexyn.Semantics.Guard;
import pexyn.Semantics.Store;

/**
 * A guard inference based on ID3. The main difference is that the algorithm
 * takes into account the cost of guards (not just their gain).
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
		implements ConditionInferencer<ExampleType, LabelType, FeatureType> {
	/**
	 * The set of Boolean attributes used to compute the classifier.
	 */
	protected final List<FeatureType> propositions;

	protected final Semantics<ExampleType, LabelType, FeatureType> domain;

	private final boolean shortCiruitEvaluationSemantics;

	public DTreeInferencer(Semantics<ExampleType, LabelType, FeatureType> domain, List<FeatureType> propositions,
			boolean shortCiruitEvaluationSemantics) {
		this.propositions = propositions;
		this.domain = domain;
		this.shortCiruitEvaluationSemantics = shortCiruitEvaluationSemantics;
	}

	@Override
	public Optional<Map<Cmd, ? extends Guard>> infer(Rel2<Cmd, Store> updateToValue) {
		Node root = new Node(updateToValue);
		var foundTree = splitNode(root);
		if (foundTree) {
			var result = generateAllClassifiers(root, updateToValue.all1());
			return Optional.of(result);
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Generates a Boolean expression from the tree for each label.
	 * 
	 * @param root
	 *            A decision tree.
	 * @param updates
	 *            The set of updates, which serve as the labels for the classifier.
	 * @return A mapping from an update to the respective guard.
	 */
	protected Map<Cmd, ? extends Guard> generateAllClassifiers(Node root, Collection<Cmd> updates) {
		var result = new HashMap<Cmd, FeatureType>();
		for (var update : updates) {
			FeatureType guard = generateClassifierForLabel(root, update);
			result.put(update, guard);
		}
		return result;
	}

	/**
	 * Generates the Boolean expression for the given update at the given node.
	 */
	protected FeatureType generateClassifierForLabel(Node node, Cmd update) {
		FeatureType result;
		if (node.pure()) {
			if (node.pureLabel() == update) {
				return domain.getTrue();
			} else {
				return null;
			}
		} else {
			FeatureType posGuard = generateClassifierForLabel(node.pos, update);
			FeatureType negGuard = generateClassifierForLabel(node.neg, update);
			if (posGuard == null && negGuard != null) {
				result = simplifiedAnd(domain.not(node.splitter), negGuard);
			} else if (negGuard == null && posGuard != null) {
				result = simplifiedAnd(node.splitter, posGuard);
			} else if (posGuard == null && negGuard == null) {
				result = null;
			} else {
				if (shortCiruitEvaluationSemantics) {
					result = domain.or(simplifiedAnd(node.splitter, posGuard), negGuard);
				} else {
					result = domain.or(simplifiedAnd(node.splitter, posGuard),
							simplifiedAnd(domain.not(node.splitter), negGuard));
				}
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
	 * 
	 * @return true if refinement succeeded (all leaves are pure) and false
	 *         otherwise.
	 */
	protected boolean splitNode(Node root) {
		if (root.pure()) {
			return true;
		} else {
			var optSplitter = findBestSplitter(root);
			if (!optSplitter.isPresent()) {
				return false;
			}
			populateNode(root, optSplitter.get());
			var subTreeSuccessfullyBuilt = splitNode(root.pos);
			if (!subTreeSuccessfullyBuilt) {
				return false;
			}
			subTreeSuccessfullyBuilt = splitNode(root.neg);
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

	protected Optional<FeatureType> findBestSplitter(Node n) {
		var updateToValue = n.labelToExample;
		var bestScore = 0f;
		FeatureType bestGuard = null;
		for (FeatureType guard : propositions) {
			var entropyForGuardPos = entropyOnSplit(updateToValue, guard, true);
			var entropyForGuardNeg = entropyOnSplit(updateToValue, guard, false);
			// This is expensive: optimize.
			var posRatio = (float) countSplitterMatches(n.labelToExample, guard, true) / n.labelToExample.size();
			var entropyReductionFromPos = entropyForGuardPos * posRatio;
			var entropyReductionFromNeg = entropyForGuardNeg * (1 - posRatio);
			var gain = n.entropy - entropyReductionFromPos - entropyReductionFromNeg;
			var score = gain / (float) domain.guardCost(guard);
			if (score > bestScore) {
				bestScore = score;
				bestGuard = guard;
			}
		}

		if (bestGuard != null) {
			return Optional.ofNullable(bestGuard);
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Populates the sub-nodes of the given node with the given splitter.
	 */
	protected void populateNode(Node node, FeatureType splitter) {
		node.splitter = splitter;
		var posVals = new HashRel2<Cmd, Store>();
		var negVals = new HashRel2<Cmd, Store>();
		for (var updateValue : node.labelToExample.all()) {
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
		var posNode = new Node(posVals);
		var negNode = new Node(negVals);
		node.pos = posNode;
		node.neg = negNode;
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
		Rel2<Cmd, Store> labelToExample;

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

		public final float entropy;

		public Node(Rel2<Cmd, Store> updateToValue) {
			this.labelToExample = updateToValue;
			this.entropy = entropyOnSplit(updateToValue, domain.getTrue(), true);
		}

		public boolean leaf() {
			return splitter == null;
		}

		public boolean pure() {
			assert labelToExample.size() > 0;
			int uniqueKeys = new HashSet<Cmd>(labelToExample.all1()).size();
			return uniqueKeys == 1;
		}

		/**
		 * Returns the only label in this node, assuming that this is a pure node.
		 */
		public Cmd pureLabel() {
			assert pure();
			var result = labelToExample.all1().iterator().next();
			return result;
		}
	}

	private int countSplitterMatches(Rel2<Cmd, Store> vals, FeatureType splitter, boolean polarity) {
		var result = 0;
		for (var pair : vals.all()) {
			@SuppressWarnings("unchecked")
			ExampleType val = (ExampleType) pair.second;
			boolean holds = domain.test(splitter, val);
			if (holds == polarity) {
				++result;
			}
		}
		return result;
	}

	/**
	 * TODO: Use Trove maps to improve efficiency.
	 * 
	 * @param vals
	 * @param splitter
	 * @return
	 */
	private float entropyOnSplit(Rel2<Cmd, Store> vals, FeatureType splitter, boolean polarity) {
		var labelToNumPos = new HashMap<Cmd, Integer>();
		int totalValsForSplitter = 0;
		for (var label : vals.all1()) {
			labelToNumPos.put(label, 0);
		}

		// Compute the proportions for each label.
		for (var pair : vals.all()) {
			@SuppressWarnings("unchecked")
			ExampleType val = (ExampleType) pair.second;
			boolean holds = domain.test(splitter, val);
			if (!polarity) {
				holds = !holds;
			}
			if (holds) {
				var label = pair.first;
				labelToNumPos.put(label, labelToNumPos.get(label) + 1);
				++totalValsForSplitter;
			}
		}

		float entropy = 0;
		for (Map.Entry<Cmd, Integer> labelCount : labelToNumPos.entrySet()) {
			int numPos = labelCount.getValue();
			if (numPos == 0) {
				// Zero entropy.
				continue;
			}
			float proportionOfClass = (float) numPos / (float) totalValsForSplitter;
			entropy -= proportionOfClass * log2(proportionOfClass);
		}
		return entropy;
	}

	private static float log2(float num) {
		return (float) (Math.log(num) / Math.log(2.0f));
	}
}
