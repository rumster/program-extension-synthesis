/** A node in a singly-linked list.
 */
type SLL {
  n:SLL
  d:int
}

/** Finds the element with the maximal value.
 */
findMax(head:SLL) -> (t:SLL) {
  var max:int
  
  t = head;
  max = head.d;
  while (t != null) {
    if (t.d > max) {
      max = t.d;
    }
    t = t.n;
  }

  example {
    [head==o0 && o0.n==o1 && o1.n==null &&
     o0.d==1 && o1.d==2] -> ...
  }

  example {
    [head==o0 && o0.n==o1 && o1.n==null &&
     o0.d==-7 && o1.d==-9] -> ...
  }

  example {
    [head==o0 && o0.n==o1 && o1.n==null &&
     o0.d==1000 && o1.d==-7] -> ...
  }

  example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==null &&
     o0.d==1 && o1.d==3 && o2.d==4] -> ...
  }

/*
  example {
    [val==0 && head==null] -> ...
  }
*/

  test example {
    [head==o0 && o0.n==null && o0.d==5] -> ...
  }

  test example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==null &&
     o0.d==1 && o1.d==-2 && o2.d==3] -> ...
  }
 
  test example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==o3 && o3.n==null &&
     o0.d==1 && o1.d==200 && o2.d==-5 && o3.d==-4] -> ...
  }
  
  test example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==o3 && o3.n==null &&
     o0.d==1 && o1.d==-200 && o2.d==0 && o3.d==4] -> ...
  }
  
  test example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==o3 && o3.n==null &&
     o0.d==-1 && o1.d==2 && o2.d==3 && o3.d==4] -> ...
  }  

  test example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==o3 && o3.n==null &&
     o0.d==1 && o1.d==2 && o2.d==3 && o3.d==0] -> ...
  }  

  test example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==o3 && o3.n==null &&
     o0.d==4 && o1.d==5 && o2.d==1 && o3.d==9] -> ...
  }
}
