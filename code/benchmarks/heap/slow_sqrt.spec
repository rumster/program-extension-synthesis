/* Computes the integer square root of x.
 * That is, the square root, rounded down.
 */ 
sqrt(x:int) -> (res:int) {
  var t:int
  var tn:int
  
  res = 1;
  t = res*res;
  tn = (res+1)*(res+1);
  while (tn < x) {
    res = res + 1;
    t = res*res;
    tn = (res+1)*(res+1);
  }

  example {
    [x==1] -> ...
  }

  example {
    [x==5] -> ...
  }

/*  
  example {
    [x==1]
    -> res = 1;
    -> t = res*res;
    -> tn = (res+1)*(res+1);
  }

  example {
    [x==5]
    -> res = 1;
    -> t = res*res;
    -> tn = (res+1)*(res+1);
    -> res = res+1;
    -> t = res*res;
    -> tn = (res+1)*(res+1);
  }
*/

  test example {
    [x==9] -> ...
  }

  test example {
    [x==25] -> ...
  }

  test example {
    [x==46] -> ...
  }

  test example {
    [x==83] -> ...
  }

  test example {
    [x==63] -> ...
  }
}
