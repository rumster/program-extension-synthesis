package heap_io;

import java.util.Random;

/**
 * Implements a singly-linked list data structure.
 * 
 * @author romanm
 */
public class SLL {
	private static final Random random = new Random(5);

	@Override
	public boolean equals(Object obj) { // so mapping will work
		return (this == obj);
	}

	public int d;
	public SLL n;

	public SLL(int d, SLL n) {
		this.d = d;
		this.n = n;
	}

	public SLL(int d) {
		this(d, null);
	}

	/**
	 * Generates an acyclic list of a specified length containing random data
	 * values.
	 */
	public static SLL genRandomAcyclic(int length) {
		Random r = new Random(5);
		SLL result = null;
		while (length > 0) {
			int v = r.nextInt(length * 10);
			SLL elem = new SLL(v, result);
			result = elem;
			--length;
		}
		return result;
	}

	public static SLL genAcyclicZeroes(int length) {
		SLL result = null;
		while (length > 0) {
			SLL elem = new SLL(0, result);
			result = elem;
			--length;
		}
		return result;
	}

	/**
	 * Generates an acyclic list of a specified length containing random data values
	 * in ascending order.
	 */
	public static SLL genAcyclicSorted(int length) {
		SLL result = null;
		int v = random.nextInt(2);
		while (length > 0) {
			int d = random.nextInt(4);
			v = v + d;
			SLL elem = new SLL(v, result);
			result = elem;
			--length;
		}
		return result;
	}

	/**
	 * Looks for a list element with the specified integer value.<br>
	 * Precondition: 'from' points to a non-null acyclic list.
	 * 
	 * @param head
	 *            The list cell to search from.
	 * @param val
	 *            The value to look for.
	 * @return A list cell in the list reachable from 'from' with the given data
	 *         value or null if there is none.
	 */
	public static SLL find(SLL head, int val) {
		SLL result = head;
		while (result != null && result.d != val) {
			result = result.n;
		}
		return result;
	}

	/**
	 * Find the list element of the maximal value that is lower than the given value
	 * in a sorted list.<br>
	 * Precondition: 'from' points to a sorted non-null acyclic list with.
	 * 
	 * @param head
	 *            The list cell to search from.
	 * @param val
	 *            The value to look for.
	 * @return A list cell in the list reachable from 'from' with the given data
	 *         value or null if there is none.
	 */
	public static SLL findMaxInSorted(SLL head, int val) {
		SLL result = head;
		while (result != null && result.d != val && result.d < val) {
			result = result.n;
		}
		return result;
	}

	/**
	 * Returns the cell with the minimal data value.<br>
	 * Precondition: 'from' points to a non-null acyclic list.
	 */
	public static SLL findMin(SLL head) {
		SLL result = head;
		SLL t = head;
		while (t != null) {
			if (t.d < result.d)
				result = t;
			t = t.n;
		}
		return result;
	}

	/**
	 * Returns the cell with the maximal data value.<br>
	 * Precondition: 'from' points to a non-null acyclic list.
	 */
	public static SLL findMax(SLL head) {
		SLL result = head;
		SLL t = head;
		while (t != null) {
			if (t.d > result.d)
				result = t;
			t = t.n;
		}
		return result;
	}

	/**
	 * Adds a cell to the beginning of the list.<br>
	 * Precondition: 'head' points to an acyclic list.
	 */
	public static SLL add(SLL head, SLL elem) {
		elem.n = head;
		return elem;
	}

	/**
	 * Adds a cell to its relative place on the list.<br>
	 * Precondition: 'head' points to a sorted acyclic list.
	 */
	public static SLL addSorted(SLL head, SLL elem) {
		if (elem.d > head.d) {
			elem.n = head;
			return elem;
		}
		SLL curr = head;
		while (curr.n != null) {
			if (elem.d >= curr.n.d) { // found the spot
				elem.n = curr.n;
				curr.n = elem;
				break;
			}
			curr = curr.n;
		}
		if (curr.n == null)
			curr.n = elem;
		return head;
	}

	/**
	 * Sets the data field to the given value.
	 */
	public static void fill(SLL head, int val) {
		SLL t = head;
		while (t != null) {
			t.d = val;
			t = t.n;
		}
	}

	/**
	 * In-situ list reversal.
	 */
	public static SLL reverse(SLL head) {
		SLL result = null;
		SLL t = head;
		while (head != null) {
			t = head.n;
			head.n = result;
			result = head;
			head = t;
		}
		return result;
	}

	public static SLL moveFirstToLast(SLL list) {
		if (list.n == null)
			return list;
		SLL result = list.n;
		list.n = null;
		SLL currNode = result;
		while (currNode.n != null) {
			currNode = currNode.n;
		}
		currNode.n = list;
		return result;
	}

	public static SLL merge(SLL first, SLL second) {
		SLL result, currNode;
		assert (first != null || second != null);
		if (first == null)
			return second;
		if (second == null)
			return first;
		if (first.d >= second.d) {
			currNode = first;
			first = first.n;
		} else {
			currNode = second;
			second = second.n;
		}
		result = currNode;
		while (first != null && second != null) {
			if (first.d >= second.d) {
				currNode.n = first;
				first = first.n;
			} else {
				currNode.n = second;
				second = second.n;
			}
			currNode = currNode.n;
		} // at this point at least first or second is null, so we can insert
			// the rest of both into currNode;
		currNode.n = (first == null ? second : first);

		return result;
	}

	public static SLL duplicate(SLL head) {
		throw new Error("implment me!");
	}
}