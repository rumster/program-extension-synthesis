package gp.separation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import gp.Domain.Guard;
import gp.Domain.Update;
import gp.Domain.Value;

/**
 * An inferencer based on decision tree machine learning algorithms such as ID3.
 * 
 * @author romanm
 *
 * @param <ValueType>
 * @param <UpdateType>
 * @param <GuardType>
 */
public class DTreeInferencer_old<ValueType extends Value, UpdateType extends Update, GuardType extends Guard>
		extends ConditionInferencer<ValueType, UpdateType, GuardType> {
	protected final List<GuardType> guards;

	public DTreeInferencer_old(List<GuardType> guards) {
		this.guards = guards;
	}

	@Override
	public List<Optional<GuardType>> inferList(List<Collection<? extends Value>> classes) {
		Node root = new Node(classes);
		buildTree(root);
		return null;
	}

	protected boolean buildTree(Node root) {
		if (root.pure()) {
			return true;
		} else {
			var optSplitter = findBestSplitter(root.classes);
			if (!optSplitter.isPresent()) {
				return false;
			}
			split(root, optSplitter.get());
			var subTreeSuccessfullyBuilt = buildTree(root.pos);
			if (!subTreeSuccessfullyBuilt) {
				return false;
			}
			subTreeSuccessfullyBuilt = buildTree(root.neg);
			if (!subTreeSuccessfullyBuilt) {
				return false;
			}
			return true;
		}
	}

	@Override
	public Optional<GuardType> infer(Collection<? extends Value> first, Collection<? extends Value> second) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<GuardType> guards() {
		return guards;
	}

	protected Optional<GuardType> findBestSplitter(List<Collection<? extends Value>> classes) {
		double rank = 0;
		GuardType guard = null;
		return Optional.empty();
	}

	protected void split(Node node, GuardType splitter) {
		var posClasses = new ArrayList<Collection<? extends Value>>();
		var negClasses = new ArrayList<Collection<? extends Value>>();
		var posNode = new Node(posClasses);
		var negNode = new Node(negClasses);
		node.splitter = splitter;
		node.pos = posNode;
		node.neg = negNode;
		bgu.cs.util.Collections.fill(posClasses, new NewListSupplier(node.classes.size()));
		bgu.cs.util.Collections.fill(negClasses, new NewListSupplier(node.classes.size()));
	}

	private final class NewListSupplier implements Supplier<Optional<Collection<? extends Value>>> {
		private int times;

		public NewListSupplier(int times) {
			this.times = times;
		}

		@Override
		public Optional<Collection<? extends Value>> get() {
			if (times >= 0) {
				return Optional.of(new ArrayList<ValueType>());
			} else {
				return Optional.empty();
			}
		}
	}

	protected static <E> int totalNumOfValues(List<Collection<? extends E>> classes) {
		var result = 0;
		for (var values : classes) {
			result += values.size();
		}
		return result;
	}

	public class Node {
		List<Collection<? extends Value>> classes;
		public GuardType splitter;
		public Node pos;
		public Node neg;

		public Node(List<Collection<? extends Value>> classes) {
			this.classes = classes;
		}

		public boolean leaf() {
			return splitter == null;
		}

		public boolean pure() {
			assert totalNumOfValues(classes) > 0;
			int numNonEmpty = 0;
			for (var valuesAtClass : classes) {
				if (!valuesAtClass.isEmpty()) {
					++numNonEmpty;
				}
			}
			return numNonEmpty > 1;
		}

		public int label() {
			assert pure();
			for (int i = 0; i < classes.size(); ++i) {
				if (!classes.get(i).isEmpty()) {
					return i;
				}
			}
			throw new Error("Encountered an empty node decision tree node!");
		}
	}
}
