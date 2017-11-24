package treeGrammar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import bgu.cs.util.BucketHeap;

/**
 * Iterates over the terms in the language of a given nonterminal in ascending
 * cost order, up to an optional cost bound.
 * 
 * @author romanm
 */
public class TreeIterator implements Iterator<InternalNode> {
	public final Nonterminal start;
	public final CostFun costFun;
	public final float costUpperBound;

	private InternalNode nextElem = null;

	private BucketHeap<Float, InternalNode> frontier;

	/**
	 * Constructs a condition iterator.
	 * 
	 * @param start
	 *            The nonterminal whose language the iterator ranges over.
	 * @param cfun
	 *            A cost function for partially ordering conditions.
	 */
	public TreeIterator(Nonterminal start, CostFun cfun) {
		this(start, cfun, -1);
	}

	/**
	 * Constructs a condition iterator.
	 * 
	 * @param start
	 *            The nonterminal whose language the iterator ranges over.
	 * 
	 * @param cfun
	 *            A cost function for partially ordering conditions.
	 * 
	 * @param upperBound
	 *            An upper bound on the cost of conditions. A negative value for
	 *            upperBound means infinity.
	 */
	public TreeIterator(Nonterminal start, CostFun cfun, float upperBound) {
		this.start = start;
		this.costFun = cfun;
		this.costUpperBound = upperBound;
		frontier = new BucketHeap<>();

		// Add the initial production right-hand sides to the frontier.
		List<InternalNode> seed = new ArrayList<>();
		seed.addAll(start.getProductions());
		for (InternalNode op : seed) {
			float cost = costFun.apply(op);
			frontier.put(cost, op);
		}
	}

	@Override
	public InternalNode next() {
		InternalNode result = nextElem;
		nextElem = null;
		advance();
		if (result == null) {
			// Either a) next has been called before ever calling advance, or b)
			// there is no next element.
			if (nextElem != null) { // Option a.
				return nextElem;
			} else { // Option b.
				throw new NoSuchElementException();
			}
		} else {
			return result;
		}
	}

	@Override
	public boolean hasNext() {
		advance();
		return nextElem != null;
	}

	protected void advance() {
		if (nextElem != null)
			return;

		while (!frontier.isEmpty()) {
			InternalNode minNode = frontier.pop();
			Nonterminal leftmostNonterminalNode = minNode.leftmostNonterminal();
			if (leftmostNonterminalNode == null) {
				nextElem = minNode;
				return;
			}

			for (InternalNode prod : leftmostNonterminalNode.getProductions()) {
				InternalNode succ = minNode.substituteLeftmost(prod);
				float succCost = costFun.apply(succ);
				if (costUpperBound < 0 || succCost > costUpperBound) {
					continue;
				}
				frontier.put(succCost, succ);
			}
		}
	}
}