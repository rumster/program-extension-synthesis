/** A node in a singly-linked list.
 */
type SLL {
  n:SLL
  d:int
}

/** Assigns the given value to each element of the list referenced by head.
 */
fill(head:SLL, val:int) -> () {
  var t:SLL
  
  t = head;
  while (t != null) {
    t.d = val;
    t = t.n;
  }

  test example {
    [val==2 && head==o0 && o0.n==o1 && o1.n==null] -> ...
  }

  example {
    [val==3 && head==o0 && o0.n==o1 && o1.n==o2 && o2.n==null] -> ...
  }

  test example {
    [val==0 && head==null] -> ...
  }

  test example {
    [val==1 && head==o0 && o0.n==null] -> ...
  }
 
  test example {
    [val==4 && head==o1 && o1.n==o2 && o2.n==o3 && o3.n==null] -> ...
  }
   
  test example {
    [val==5 && head==o1 && o1.n==o2 && o2.n==o3 && o3.n==o4 && o4.n==null] -> ...
  }

  test example {
    [val==6 && head==o1 && o1.n==o2 && o2.n==o3 && o3.n==o4 && o4.n==o5 && o5.n==null] -> ...
  }

  test example {
    [val==7 && head==o1 && o1.n==o2 && o2.n==o3 && o3.n==o4 && o4.n==o5 && o5.n==o6 && o6.n==null] -> ...
  }

  test example {
    [val==8 && head==o1 && o1.n==o2 && o2.n==o3 && o3.n==o4 && o4.n==o5 && o5.n==o6 && o6.n==o7 &&
     o7.n==null] -> ...
  }

  test example {
    [val==9 && head==o1 && o1.n==o2 && o2.n==o3 && o3.n==o4 && o4.n==o5 && o5.n==o6 && o6.n==o7 &&
     o7.n==o8 && o8.n==null] -> ...
  }

  test example {
    [val==10 && head==o1 && o1.n==o2 && o2.n==o3 && o3.n==o4 && o4.n==o5 && o5.n==o6 && o6.n==o7 &&
     o7.n==o8 && o8.n==o9 && o9.n==null] -> ...
  }

  test example {
    [val==11 && head==o1 && o1.n==o2 && o2.n==o3 && o3.n==o4 && o4.n==o5 && o5.n==o6 && o6.n==o7 &&
     o7.n==o8 && o8.n==o9 && o9.n==o10 && o10.n==null] -> ...
  }
}
