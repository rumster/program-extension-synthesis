/* A cell on a tape.
 */
type Cell {
  r:Cell
  l:Cell
  // The value at this cell on the tape.
  // The value -1 stands for the blank symbol.
  c:int
}

/* Simulates a Turing machine that flips the bits on
 * the input tape.
 */ 
flip(start:Cell) -> () {
  var head:Cell

  example {
    [start==o2 && 
     o1.c==-1 && o2.c==0 && o3.c==1 && o4.c==1 && o5.c==0 &&
     o6.c==-1 &&
     o2.l==o1 &&
     o1.r==o2 && o2.r==o3 && o3.r==o4 && o4.r==o5 && o5.r==o6]
    -> head = start;
    -> head.c=1; head=head.r;
    -> head.c=0; head=head.r;
    -> head.c=0; head=head.r;
    -> head.c=1; head=head.r;
  }
}
