/* Computes the integer square root of x (the square root rounded down)
 * via binary search.
 */ 
sqrt(n:int) -> (res:int) {
  var l:int
  var h:int
  var m:int
  
  l = 0;
  h = n;
  while (h - l > 1) {	
    m = (l + h) / 2;
	if (m * m > n) {
      h = m;
	}
	else {
      l = m;
    }
  }
  res = h;
  if (res * res > n) {
    res = res - 1;
  }

  example {
    [n==1] -> ...
  }

  example {
    [n==5] -> ...
  }

  example {
    [n==9] -> ...
  }

  example {
    [n==12] -> ...
  }

  test example {
    [n==18] -> ...
  }

  test example {
    [n==25] -> ...
  }
  
  test example {
    [n==37] -> ...
  }

  test example {
    [n==46] -> ...
  }
  
  test example {
    [n==52] -> ...
  }  

  test example {
    [n==63] -> ...
  }
  
  test example {
    [n==71] -> ...
  }  

  test example {
    [n==83] -> ...
  }
  
  test example {
    [n==91] -> ...
  }
  
  test example {
    [n==127] -> ...
  }
}
