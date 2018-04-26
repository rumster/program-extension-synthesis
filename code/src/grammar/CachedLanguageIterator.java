package grammar;

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
public class CachedLanguageIterator implements Iterator<Node> {
	public final Nonterminal start;
	public final CostFun costFun;
	public final float costUpperBound;

	private List<Node> cache;
	private int nextElem;

	private BucketHeap<Float, Node> frontier;

	/**
	 * Constructs a condition iterator.
	 * 
	 * @param start
	 *            The nonterminal whose language the iterator ranges over.
	 * @param cfun
	 *            A cost function for partially ordering conditions.
	 */
	public CachedLanguageIterator(Nonterminal start, CostFun cfun) {
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
	public CachedLanguageIterator(Nonterminal start, CostFun cfun, float upperBound) {
		this.start = start;
		this.costFun = cfun;
		this.costUpperBound = upperBound;
		frontier = new BucketHeap<>();
		
		cache = new ArrayList<>();
		nextElem = 0;
		
		// Add the initial production right-hand sides to the frontier.
		List<Node> seed = new ArrayList<>();
		seed.addAll(start.productions());
		for (Node op : seed) {
			float cost = costFun.apply(op);
			frontier.put(cost, op);
		}
	}

	@Override
	public Node next() {
		if(nextElem >= cache.size()){
			// Either a) next has been called before ever calling advance, or b)
			// there is no next element.
			advance();
			if (nextElem < cache.size()){ // Option a.
				Node result = cache.get(nextElem);
				nextElem++;	
				return result;
			}
			else // Option b.
				throw new NoSuchElementException();
		}
		Node result = cache.get(nextElem);
		nextElem++;	
		advance();
		return result;
	}

	@Override
	public boolean hasNext() {
		advance();
		return nextElem < cache.size();
	}
	
	public boolean has(int ind) {
		boolean result = false;
		if(ind >= 0 && ind < nextElem){
			result = true; 
		}
		else if(ind == nextElem)
		{
			advance();
			result = nextElem < cache.size();
		}		
		return result;
	}
	
	public void reset(){
		reset(0);
	}
	
	private void reset(int index){
		if(index > nextElem){
			throw new ArrayIndexOutOfBoundsException();
		}
		nextElem = index;
	}
	
	public Node get(int ind){
		if(ind < 0 || ind > nextElem){
			throw new ArrayIndexOutOfBoundsException();
		}
		Node result;
		if(ind < nextElem){
			result = cache.get(ind);
		}
		else{
			result = next();
		}
		return result;
	}
	
	public List<Node> get(int from, int to){
		if(from < 0 || to < 0 || to > nextElem || from > nextElem || to < from){
			throw new ArrayIndexOutOfBoundsException();
		}
		return cache.subList(from, to+1);
	}

	protected void advance() {
		if (nextElem < cache.size())
			return;

		while (!frontier.isEmpty()) {
			Node minNode = frontier.pop();
			Nonterminal leftmostNonterminalNode = minNode.leftmostNonterminal();
			if (leftmostNonterminalNode == null) {
				cache.add(minNode);
				return;
			}

			for (Node prod : leftmostNonterminalNode.productions()) {
				Node succ = minNode.substituteLeftmostNonterminal(prod);
				float succCost = costFun.apply(succ);
				if (costUpperBound < 0 || succCost > costUpperBound) {
					continue;
				}
				frontier.put(succCost, succ);
			}
		}
	}
}
