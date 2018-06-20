/** A node in a singly-linked list.
 */
type SLL {
  n:SLL
  d:int
}

/** Reverses the list referenced by head in-place.
 */
reverse(mut head:SLL) -> (result:SLL) {
  var t:SLL
  
  // The target code.
  result = null;
  while (head != null) {
    t = head.n;
    head.n = result;
    result = head;
    head = t;    
  }
  
  // Intuitively, we want to lear the following parallel assignment:
  // t, head.n, result, head = head.n, result, head, t

  // The only example that is really needed.
  example {
    [head==o0 && o0.n==o1 && o1.n==null] ->
    [result==null] ->
    [head==null && o0.n==null && o1.n==o0 && result==o1 && t==null]
  }

  test example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==null] -> ...
  }

  test example {
    [head==null] -> ...
  }

  test example {
    [head==o0 && o0.n==null] -> ...
  }
 
  test example {
    [head==o1 && o1.n==o2 && o2.n==o3 && o3.n==null] -> ...
  }
   
  test example {
    [head==o1 && o1.n==o2 && o2.n==o3 && o3.n==o4 && o4.n==null] -> ...
  }

  test example {
    [head==o1 && o1.n==o2 && o2.n==o3 && o3.n==o4 && o4.n==o5 && o5.n==null] -> ...
  }

  test example {
    [head==o1 && o1.n==o2 && o2.n==o3 && o3.n==o4 && o4.n==o5 && o5.n==o6 && o6.n==null] -> ...
  }

  test example {
    [head==o1 && o1.n==o2 && o2.n==o3 && o3.n==o4 && o4.n==o5 && o5.n==o6 && o6.n==o7 &&
     o7.n==null] -> ...
  }

  test example {
    [head==o1 && o1.n==o2 && o2.n==o3 && o3.n==o4 && o4.n==o5 && o5.n==o6 && o6.n==o7 &&
     o7.n==o8 && o8.n==null] -> ...
  }

  test example {
    [head==o1 && o1.n==o2 && o2.n==o3 && o3.n==o4 && o4.n==o5 && o5.n==o6 && o6.n==o7 &&
     o7.n==o8 && o8.n==o9 && o9.n==null] -> ...
  }

  test example {
    [head==o1 && o1.n==o2 && o2.n==o3 && o3.n==o4 && o4.n==o5 && o5.n==o6 && o6.n==o7 &&
     o7.n==o8 && o8.n==o9 && o9.n==o10 && o10.n==null] -> ...
  }
}