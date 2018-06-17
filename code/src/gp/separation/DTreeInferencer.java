package gp.separation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import bgu.cs.util.rel.HashRel2;
import bgu.cs.util.rel.Rel2;
import gp.Domain;
import gp.Plan;
import gp.Domain.Guard;
import gp.Domain.Update;
import gp.Domain.Value;

/**
 * An inferencer based on learning a decision tree. The updates serve as the
 * labels of the learned classifier and the values store as the samples that
 * need to be classified.
 * 
 * @author romanm
 *
 * @param <ValueType>
 * @param <UpdateType>
 * @param <GuardType>
 */
public class DTreeInferencer<ValueType extends Value, UpdateType extends Update, GuardType extends Guard>
		extends ConditionInferencer<ValueType, UpdateType, GuardType> {
	/**
	 * The set of Boolean attributes used to compute the classifier.
	 */
	protected final List<GuardType> propositions;

	protected final Domain<ValueType, UpdateType, GuardType> domain;

	public DTreeInferencer(Domain<ValueType, UpdateType, GuardType> domain, List<Plan<ValueType, UpdateType>> plans) {
		this.propositions = domain.generateBasicGuards(plans);
		this.domain = domain;
	}

	@Override
	public List<Optional<GuardType>> inferList(List<Collection<? extends Value>> classes) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<GuardType> infer(Collection<? extends Value> first, Collection<? extends Value> second) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<Map<Update, ? extends Guard>> infer(Rel2<Update, Value> updateToValue) {
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
	protected Map<Update, ? extends Guard> compileGuards(Node root, Collection<Update> updates) {
		var result = new HashMap<Update, GuardType>();
		for (var update : updates) {
			GuardType guard = compileGuard(root, update);
			result.put(update, guard);
		}
		return result;
	}

	/**
	 * Generates the Boolean expression for the given update at the given node.
	 */
	protected GuardType compileGuard(Node node, Update update) {
		GuardType result;
		if (node.pure()) {
			if (node.pureLabel() == update) {
				return domain.getTrue();
			} else {
				return null;
			}
		} else {
			var posGuard = compileGuard(node.pos, update);
			var negGuard = compileGuard(node.neg, update);
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
	protected GuardType simplifiedAnd(GuardType first, GuardType second) {
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
	public List<GuardType> guards() {
		return propositions;
	}

	protected Optional<GuardType> findBestSplitter(Rel2<Update, Value> updateToValue, Update updateToSplit) {
		// double numOfValues = updateToValue.size();
		var bestScore = 0d;
		GuardType bestGuard = null;
		for (var guard : propositions) {
			// var gini = 0d;
			// int numOfSatisfiedValues = 0;
			// for (var val : updateToValue.all2()) {
			// @SuppressWarnings("unchecked")
			// boolean holds = domain.test(guard, (ValueType) val);
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
				var val = (ValueType) pair.second;
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
			var score = updateToSplitRatio
					* (1 - nonUpdateToSplitRatio);
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
	protected void split(Node node, GuardType splitter) {
		var posVals = new HashRel2<Update, Value>();
		var negVals = new HashRel2<Update, Value>();
		var posNode = new Node(posVals);
		var negNode = new Node(negVals);
		node.splitter = splitter;
		node.pos = posNode;
		node.neg = negNode;
		for (var updateValue : node.updateToValue.all()) {
			var update = updateValue.first;
			var value = updateValue.second;
			@SuppressWarnings("unchecked")
			boolean holds = domain.test(splitter, (ValueType) value);
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
		Rel2<Update, Value> updateToValue;

		/**
		 * The basic proposition used to split the values at this node into the
		 * sub-nodes.
		 */
		public GuardType splitter;

		/**
		 * The node into which the subset of value that satisfy the splitter are sent.
		 */
		public Node pos;

		/**
		 * The node into which the subset of value that do not satisfy the splitter are
		 * sent.
		 */
		public Node neg;

		public Node(Rel2<Update, Value> updateToValue) {
			this.updateToValue = updateToValue;
		}

		public boolean leaf() {
			return splitter == null;
		}

		public boolean pure() {
			assert updateToValue.size() > 0;
			int uniqueKeys = new HashSet<Update>(updateToValue.all1()).size();
			return uniqueKeys == 1;
		}

		public Update pureLabel() {
			assert pure();
			var result = updateToValue.all1().iterator().next();
			return result;
		}
	}
}
