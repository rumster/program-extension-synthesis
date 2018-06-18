/* Computes the integer square root of x (the square root rounded down)
 * via binary search.
 */ 
sqrt(n:int) -> (res:int) {
  var low:int
  var high:int
  var middle:int
  
  low = 0;
  high = n;
  while (high - low > 1) {	
    middle = (low + high) / 2;
	if (middle * middle > n) {
      high = middle;
	}
	else {
      low = middle;
    }
  }
  res = high;
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
