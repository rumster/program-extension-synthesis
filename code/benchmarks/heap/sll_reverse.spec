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

  // The only example that is really needed.
  example {
    [head==o0 && o0.n==o1 && o1.n==null]
    -> result=null;
    -> t=head.n;
    -> head.n=result;
    -> result=head;
    -> head=t;
    -> t=head.n;
    -> head.n=result;
    -> result=head;
    -> head=t;
  }

  example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==null]
    -> result=null;
    -> t=head.n;
    -> head.n=result;
    -> result=head;
    -> head=t;
    -> t=head.n;
    -> head.n=result;
    -> result=head;
    -> head=t;
    -> t=head.n;
    -> head.n=result;
    -> result=head;
    -> head=t;
  }

  test example {
    [head==null]
    -> result=null;
  }

  test example {
    [head==o0 && o0.n==null]
    -> result=null;
    -> t=head.n;
    -> head.n=result;
    -> result=head;
    -> head=t;
  }

  test example {
    [head==o1 && o1.n==o2 && o2.n==null] -> ...
  }
 
  test example {
    [head==o1 && o1.n==o2 && o2.n==o3 && o3.n==o4 && o4.n==null] -> ...
  }
}
