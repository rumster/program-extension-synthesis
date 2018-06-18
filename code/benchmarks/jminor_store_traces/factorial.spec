/** Computes the factorial of x in y by iteratively decrementing
 * x and multiplying its values with y.
 */
factorial(mut x:int) -> (y:int) {
  y = 1;
  while (x != 1) {
    y = y * x;
    x = x - 1;
  }

  example {
    [x==0]
    -> [x==0 && y==1]
  }
  
/*  
  example {
    [x==3]
    ->  y = 1;
    ->  y = y * x;
    ->  x = x - 1;
    ->  y = y * x;
    ->  x = x - 1;
  }
*/  
  
  example {
    [x==3]
    -> [x==3 && y==1]
    -> [x==3 && y==3]
    -> [x==2 && y==3]
    -> [x==2 && y==6]
    -> [x==1 && y==6]
  }  
    
  example {
    [x==4]
    -> [x==4 && y==1]
    -> [x==4 && y==4]
    -> [x==3 && y==4]
    -> [x==3 && y==12]
    -> [x==2 && y==12]
    -> [x==2 && y==24]
    -> [x==1 && y==24]
  }  
}
