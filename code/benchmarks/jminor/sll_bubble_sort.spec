/** A node in a singly-linked list.
 */
type SLL {
  n:SLL
  d:int
}

/** Bubble sort.
 */
sort(mut head:SLL) -> () {
  var y:SLL
  var yn:SLL
  var p:SLL  
  var t:SLL
  var change:boolean
  
//if (head == null) { 
//  return;
//}
change = true;
while (change) {
  p = null;
  change = false;
  y = head;
  yn = y.n;
  while (yn != null) {
    if (y.d > yn.d) {
      change = true;
      t = yn.n;
      y.n = t;
      yn.n = y;
      if (p == null) {
        head = yn;
      }
      else {
        p.n = yn; 
      }
      p = yn;
      yn = t;
    }
    else {
      p = y;
      y = yn;
      yn = y.n;
   }
  }
}
//return head;

  example {
    [head==o0 && o0.n==o1 && o1.n==null &&
     o0.d==1 && o1.d==2] -> ...
  }

  example {
    [head==o0 && o0.n==o1 && o1.n==null &&
     o0.d==1 && o1.d==0] -> ...
  }

  example {
    [head==o0 && o0.n==o1 && o1.n==null &&
     o0.d==0 && o1.d==1] -> ...
  }

  example {
    [head==o0 && o0.n==o1 && o1.n==null &&
     o0.d==1 && o1.d==2] -> ...
  }

  example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==null &&
     o0.d==1 && o1.d==3 && o2.d==4] -> ...
  }

/*
  example {
    [head==null] -> ...
  }
*/  

  example {
    [head==o0 && o0.n==null && o0.d==5] -> ...
  }

  example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==null &&
     o0.d==1 && o1.d==-2 && o2.d==3] -> ...
  }

  example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==null &&
     o0.d==1 && o1.d==2 && o2.d==3] -> ...
  }

  example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==null &&
     o0.d==3 && o1.d==2 && o2.d==1] -> ...
  }

  example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==null &&
     o0.d==3 && o1.d==1 && o2.d==2] -> ...
  }
 
  example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==null &&
     o0.d==1 && o1.d==3 && o2.d==2] -> ...
  }

  example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==null &&
     o0.d==-1 && o1.d==-3 && o2.d==-2] -> ...
  }

  example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==null &&
     o0.d==1 && o1.d==-3 && o2.d==-2] -> ...
  }
  
  example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==o3 && o3.n==null &&
     o0.d==1 && o1.d==2 && o2.d==3 && o3.d==4] -> ...
  }    

  example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==o3 && o3.n==null &&
     o0.d==3 && o1.d==5 && o2.d==4 && o3.d==3] -> ...
  }
  
  example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==o3 && o3.n==null &&
     o0.d==1 && o1.d==2 && o2.d==4 && o3.d==4] -> ...
  }    

  example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==o3 && o3.n==null &&
     o0.d==5 && o1.d==4 && o2.d==2 && o3.d==2] -> ...
  }    
 
  test example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==o3 && o3.n==null &&
     o0.d==-9 && o1.d==200 && o2.d==-5 && o3.d==-4] -> ...
  }

  test example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==o3 && o3.n==null &&
     o0.d==-5 && o1.d==200 && o2.d==-5 && o3.d==-4] -> ...
  }

  test example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==o3 && o3.n==null &&
     o0.d==-210 && o1.d==200 && o2.d==-5 && o3.d==-4] -> ...
  }
  
  test example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==o3 && o3.n==null &&
     o0.d==1 && o1.d==-200 && o2.d==0 && o3.d==4] -> ...
  }
  
  test example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==o3 && o3.n==null &&
     o0.d==0 && o1.d==2 && o2.d==0 && o3.d==4] -> ...
  }  

  test example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==o3 && o3.n==null &&
     o0.d==1 && o1.d==2 && o2.d==3 && o3.d==0] -> ...
  }  

  test example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==o3 && o3.n==null &&
     o0.d==1 && o1.d==2 && o2.d==3 && o3.d==-9] -> ...
  }  

  test example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==o3 && o3.n==null &&
     o0.d==-1 && o1.d==-2 && o2.d==-3 && o3.d==-9] -> ...
  }  

  test example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==o3 && o3.n==null &&
     o0.d==2 && o1.d==2 && o2.d==2 && o3.d==2] -> ...
  }  

  test example {
    [head==o0 && o0.n==o1 && o1.n==o2 && o2.n==o3 && o3.n==null &&
     o0.d==4 && o1.d==5 && o2.d==1 && o3.d==9] -> ...
  }    
}