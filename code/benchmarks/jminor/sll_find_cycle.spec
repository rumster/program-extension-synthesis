/** A node in a singly-linked list.
 */
type SLL {
  n:SLL
  d:int
}

/** Returns the root of a panhandle list.
 */
findCycle(head:SLL) -> (res:SLL) {
  var slow:SLL
  var fast:SLL
  
  slow = head.n;
  fast = head.n.n;
  while (slow != fast) {
    slow = slow.n;
    fast = fast.n.n;
  }
  slow = head;
  while (slow != fast) {
    slow = slow.n;
    fast = fast.n;
  }
  res = slow;

  example {
    [head==o0 && o0.n==o1 && o1.n==o2 &&
     o2.n==o3 && o3.n==o4 && o4.n==o2] -> ...
  }
}
