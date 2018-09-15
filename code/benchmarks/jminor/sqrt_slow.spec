/* Computes the integer square root of x (the square root rounded down)
 * by linear iteration.
 */ 
sqrtSlow(x:int) -> (res:int) {
  // We need this variable to help the condition inferencer.
  var resNext:int
   
  res = 1;
  resNext = res + 1;
  while ((res + 1) * (res + 1) < x) {
    res = res + 1;
    resNext = res + 1;
  }

  example {
    [x==1] -> ...
  }

  example {
    [x==5] -> ...
  }

  example {
    [x==9] -> ...
  }
  
  test example {
    [x==18] -> ...
  }

  test example {
    [x==25] -> ...
  }
  
  test example {
    [x==37] -> ...
  }

  test example {
    [x==46] -> ...
  }
  
  test example {
    [x==52] -> ...
  }  

  test example {
    [x==63] -> ...
  }
  
  test example {
    [x==71] -> ...
  }  

  test example {
    [x==83] -> ...
  }
  
  test example {
    [x==91] -> ...
  }
  
  test example {
    [x==127] -> ...
  }
}
