type SLL {
  n:SLL
  d:int
}

merge(mut first:SLL, mut second:SLL) -> (result:SLL) {
  var t:SLL
  
  result = null;
  while (first != null || second != null) {
    t = result;
    if (first == null || first != null && second != null && second.d < first.d) {     
      result = second;
      second = second.n;      
    }
    else {
      result = first;
      first = first.n;
    }
    result.n = t; 
  }

  example {
    [first==a0 && a0.n==null &&
     a0.d==1 &&
     second==b0 && b0.n==null &&
     b0.d==2
    ]
    
    -> result=null;
    
    -> t=result;
    -> result=first;
    -> first=first.n;
    -> result.n=t;

    -> t=result;
    -> result=second;
    -> second=second.n;
    -> result.n=t;
  }

  example {
    [first==a0 && a0.n==null &&
     a0.d==2 &&
     second==b0 && b0.n==null &&
     b0.d==1
    ]
    
    -> result=null;
    
    -> t=result;
    -> result=second;
    -> second=second.n;
    -> result.n=t;
    
    -> t=result;
    -> result=first;
    -> first=first.n;
    -> result.n=t;    
  }

  example {
    [first==a0 && a0.n==a1 && a1.n==null &&
     a0.d==2 && a1.d==4 &&
     second==b0 && b0.n==b1 && b1.n==null &&
     b0.d==1 && b1.d==3
    ]
    
    -> result=null;
    
    -> t=result;
    -> result=second;
    -> second=second.n;
    -> result.n=t;

    -> t=result;
    -> result=first;
    -> first=first.n;
    -> result.n=t;

    -> t=result;
    -> result=second;
    -> second=second.n;
    -> result.n=t;

    -> t=result;
    -> result=first;
    -> first=first.n;
    -> result.n=t;
  }

  test example {
    [first==a0 && a0.n==a1 && a1.n==null &&
     a0.d==1 && a1.d==3 &&
     second==b0 && b0.n==b1 && b1.n==null &&
     b0.d==2 && b1.d==4
    ] -> ...
  }
}
