package heap_io;

import java.util.Random;

/**
 * Implements a doubly-linked list.
 * 
 * @author Shooki Matzliah
 */
public class DLL {
	public DLL p;
	public DLL n;
	public int d;

	@Override
	public String toString() {
		return toString(this);
	}

	public String toString(DLL end) {
		if (n == null || n == end)
			return "[" + d + "]";
		else
			return "[" + d + "] <-> " + n.toString(end);
	}

	@Override
	public boolean equals(Object obj) {
		return (this == obj);
	}

	public DLL(int d, DLL n) {
		this.d = d;
		this.n = n;
	}

	public DLL(int d) {
		this(d, null);
	}

	/**
	 * Generates an acyclic list of a specified length containing random data
	 * values.
	 */
	public static DLL genAcyclic(int length) {
		Random r = new Random(5);
		DLL result = null;
		while (length > 0) {
			int v = r.nextInt();
			DLL elem = new DLL(v, result);
			if (result != null)
				result.p = elem;
			result = elem;
			--length;
		}
		return result;
	}

	/**
	 * Generates an acyclic list of a specified length containing random data values
	 * in ascending order.
	 */
	public static DLL genCyclicSorted(int length) {
		DLL head = genAcyclicSorted(length);
		DLL end = head;
		while (end.n != null)
			end = end.n;
		end.n = head;
		head.p = end;
		return head;

	}

	/**
	 * Generates an acyclic list of a specified length containing random data values
	 * in ascending order.
	 */
	public static DLL genAcyclicSorted(int length) {
		Random r = new Random(5);
		DLL result = null;
		int v = r.nextInt(100);
		while (length > 0) {
			int d = r.nextInt(20);
			v = v + d;
			DLL elem = new DLL(v, result);
			if (result != null)
				result.p = elem;
			result = elem;
			--length;
		}
		return result;
	}

	/**
	 * Looks for a list element with the specified integer value.<br>
	 * Precondition: 'from' points to a non-null Cyclic list.
	 * 
	 * @param from
	 *            The list cell to search from.
	 * @param val
	 *            The value to look for.
	 * @return A list cell in the list reachable from 'from' with the given data
	 *         value or null if there is none.
	 */
	public static DLL findInCyclic(DLL from, int val) {
		DLL curr = from.n;
		while (curr != from) {
			if (curr.d == val)
				return curr;
			curr = curr.n;
		}
		return null;
	}

	/**
	 * Looks for a list element with the specified integer value through the forward
	 * pointers.<br>
	 * Precondition: 'from' points to a non-null acyclic list.
	 * 
	 * @param from
	 *            The list cell to search from.
	 * @param val
	 *            The value to look for.
	 * @return A list cell in the list reachable from 'from' with the given data
	 *         value or null if there is none.
	 */
	public static DLL find(DLL from, int val) {
		DLL result = from;
		while (result != null) {
			if (result.d == val)
				break;
			result = result.n;
		}
		return result;
	}

	/**
	 * Looks for a list element with the specified integer value through the
	 * backward pointers.<br>
	 * Precondition: 'from' points to a non-null acyclic list.
	 * 
	 * @param from
	 *            The list cell to search from.
	 * @param val
	 *            The value to look for.
	 * @return A list cell in the list reachable from 'from' with the given data
	 *         value or null if there is none.
	 */
	public static DLL findReverse(DLL from, int val) {
		DLL result = from;
		while (result != null) {
			if (result.d == val)
				break;
			result = result.p;
		}
		return result;
	}

	/**
	 * Find the list element of the maximal value that is lower than the given value
	 * in a sorted list.<br>
	 * Precondition: 'from' points to a sorted non-null acyclic list with.
	 * 
	 * @param from
	 *            The list cell to search from.
	 * @param val
	 *            The value to look for.
	 * @return A list cell in the list reachable from 'from' with the given data
	 *         value or null if there is none.
	 */
	public static DLL findMaxInSorted(DLL from, int val) {
		DLL result = from;
		while (result != null && result.d != val && result.d < val) {
			result = result.n;
		}
		return result;
	}

	/**
	 * Returns the cell with the minimal data value.<br>
	 * Precondition: 'from' points to a non-null acyclic list.
	 */
	public static DLL findMin(DLL from) {
		DLL result = from;
		DLL t = from;
		while (t != null) {
			if (t.d < result.d)
				result = t;
			t = t.n;
		}
		return result;
	}

	/**
	 * Returns the cell with the minimal data value.<br>
	 * Precondition: 'from' points to a non-null acyclic list.
	 */
	public static DLL findMinInCyclic(DLL from) {
		DLL result = from;
		DLL t = from.n;
		while (t != from) {
			if (t.d < result.d)
				result = t;
			t = t.n;
		}
		return result;
	}

	/**
	 * Adds a cell to the beginning of the list.<br>
	 * Precondition: 'head' points to an acyclic list.
	 */
	public static DLL add(DLL head, DLL elem) {
		elem.n = head;
		head.p = elem;
		return elem;
	}

	/**
	 * Adds a cell to its relative place on the list.<br>
	 * Precondition: 'head' points to a sorted acyclic list.
	 */
	public static DLL addSorted(DLL head, DLL elem) {
		if (elem.d > head.d) {
			elem.n = head;
			head.p = elem;
			return elem;
		}
		DLL curr = head;
		while (curr.n != null) {
			if (elem.d >= curr.n.d) { // found the spot
				elem.n = curr.n;
				curr.n.p = elem;
				curr.n = elem;
				elem.p = curr;
				break;
			}
			curr = curr.n;
		}
		if (curr.n == null) {
			curr.n = elem;
			elem.p = curr;
		}
		return head;
	}

	// precondition: list is acyclic
	public static DLL moveFirstToLast(DLL list) {
		if (list.n == null)
			return list;
		DLL result = list.n;
		list.n = null;
		DLL currNode = result;
		currNode.p = null;
		while (currNode.n != null) {
			currNode = currNode.n;
		}
		currNode.n = list;
		list.p = currNode;
		return result;
	}

	public static DLL merge(DLL first, DLL second) {
		DLL result, currNode;
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
				first.p = currNode;
				first = first.n;
			} else {
				currNode.n = second;
				second.p = currNode;
				second = second.n;
			}
			currNode = currNode.n;
		} // at this point at least first or second is null, so we can insert
			// the rest of both into currNode;
		if (second == null) {
			currNode.n = first;
			first.p = currNode;
		} else {
			currNode.n = second;
			second.p = currNode;
		}

		return result;
	}
}