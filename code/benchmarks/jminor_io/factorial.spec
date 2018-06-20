/** Computes the factorial of x in y by iteratively decrementing
 * x and multiplying its values with y.
 */
factorial(mut x:int) -> (y:int) {
  y = 1;
  while (x != 1) {
    y = y * x;
    x = x - 1;
  }
  
  // Intuitively, we would like to learn the parallel assignment y,x = y*x, x-1.

  example {
    [x==0]
    -> [x==0 && y==1]
  }
  
  example {
    [x==3]
    -> [y==1]
    -> [x==1 && y==6]
  }  
    
  example {
    [x==4]
    -> [y==1]
    -> [x==1 && y==24]
  }  
}
