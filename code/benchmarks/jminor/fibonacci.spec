/** An iterative algorithm for computing the n'th
 * Fibonacci number.
 */
fibonacci(n:int) -> (fib:int) {
  var i:int
  var prevFib:int
  var temp:int
  
  if (n == 0) {
    fib = 1;
  }
  else {
    fib = 1;
    prevFib = 1;
    i = 2;
    while (i < n) {
      temp = fib;
      fib = fib + prevFib;
      prevFib = temp;
      i = i + 1;
    }    
  }

  example {
    [n==0] -> ...
  }

  test example {
    [n==1] -> ...
  }

  test example {
    [n==2] -> ...
  }
  
  example {
    [n==3] -> ...
  }
  
  test example {
    [n==4] -> ...
  }  

  example {
    [n==5] -> ...
  }
  
  test example {
    [n==6] -> ...
  }
  
  test example {
    [n==7] -> ...
  }

  test example {
    [n==8] -> ...
  }
  
  test example {
    [n==9] -> ...
  }
  
  test example {
    [n==10] -> ...
  }
}
