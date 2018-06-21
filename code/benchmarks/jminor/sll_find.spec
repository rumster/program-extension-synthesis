/** A node in a singly-linked list.
 */
type SLL {
  n:SLL
  d:int
}

/** Returns the node with the given data value, if it exists, and null otherwise.
 */
find(head:SLL, val:int) -> (t:SLL) {
  t = head;
  while (t != null && t.d != val) {
    t = t.n;
  }

  example {
    [val==2 && head==o0 && o0.n==o1 && o1.n==null &&
     o0.d==1 && o1.d==2] -> ...
  }

  test example {
    [val==0 && head==o0 && o0.n==o1 && o1.n==null &&
     o0.d==1 && o1.d==0] -> ...
  }

  test example {
    [val==0 && head==o0 && o0.n==o1 && o1.n==null &&
     o0.d==0 && o1.d==1] -> ...
  }

  test example {
    [val==0 && head==o0 && o0.n==o1 && o1.n==null &&
     o0.d==1 && o1.d==2] -> ...
  }

  example {
    [val==3 && head==o0 && o0.n==o1 && o1.n==o2 && o2.n==null &&
     o0.d==1 && o1.d==3 && o2.d==4] -> ...
  }

  example {
    [val==0 && head==null] -> ...
  }

  example {
    [val==1 && head==o0 && o0.n==null && o0.d==5] -> ...
  }

  example {
    [val==4 && head==o0 && o0.n==o1 && o1.n==o2 && o2.n==null &&
     o0.d==1 && o1.d==-2 && o2.d==3] -> ...
  }
 
  test example {
    [val==-5 && head==o0 && o0.n==o1 && o1.n==o2 && o2.n==o3 && o3.n==null &&
     o0.d==-9 && o1.d==200 && o2.d==-5 && o3.d==-4] -> ...
  }

  test example {
    [val==-5 && head==o0 && o0.n==o1 && o1.n==o2 && o2.n==o3 && o3.n==null &&
     o0.d==-5 && o1.d==200 && o2.d==-5 && o3.d==-4] -> ...
  }

  test example {
    [val==200 && head==o0 && o0.n==o1 && o1.n==o2 && o2.n==o3 && o3.n==null &&
     o0.d==-210 && o1.d==200 && o2.d==-5 && o3.d==-4] -> ...
  }
  
  example {
    [val==0 && head==o0 && o0.n==o1 && o1.n==o2 && o2.n==o3 && o3.n==null &&
     o0.d==1 && o1.d==-200 && o2.d==0 && o3.d==4] -> ...
  }
  
  example {
    [val==5 && head==o0 && o0.n==o1 && o1.n==o2 && o2.n==o3 && o3.n==null &&
     o0.d==0 && o1.d==2 && o2.d==0 && o3.d==4] -> ...
  }  

  example {
    [val==0 && head==o0 && o0.n==o1 && o1.n==o2 && o2.n==o3 && o3.n==null &&
     o0.d==1 && o1.d==2 && o2.d==3 && o3.d==0] -> ...
  }  

  example {
    [val==0 && head==o0 && o0.n==o1 && o1.n==o2 && o2.n==o3 && o3.n==null &&
     o0.d==1 && o1.d==2 && o2.d==3 && o3.d==-9] -> ...
  }  

  example {
    [val==0 && head==o0 && o0.n==o1 && o1.n==o2 && o2.n==o3 && o3.n==null &&
     o0.d==4 && o1.d==5 && o2.d==1 && o3.d==9] -> ...
  }    
}
