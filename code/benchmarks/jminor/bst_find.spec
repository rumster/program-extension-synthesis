/** A binary tree node.
 */
type Node {
  left:Node
  right:Node
  d:int
}

/** Finds a key in a binary search tree.
 */ 
find(root:Node, val:int) -> (res:Node) {
  var t:Node

  example {
    [root==o1 &&
     o1.d==7 &&
     val==7]
    -> t = root;
    -> res = t;
  }

  example {
    [root==o1 && o1.right==o2 && o2.left==o3 &&
     o1.d==21 && o2.d==23 && o3.d==22 && 
     val==22]
    -> t = root;
    -> t = t.right;
    -> t = t.left;
    -> res = t;
  }

  example {
    [root==o1 && o1.left==o2 && o2.right==o3 &&
     o1.d==13 && o2.d==11 && o3.d==12 && 
     val==12]
    -> t = root;
    -> t = t.left;
    -> t = t.right;
    -> res = t;
  }

  example {
    [root==o1 && o1.right==o2 && o2.left==o3 &&
     o3.right==o4 && o4.left==o5 &&
     o1.d==11 && o2.d==100 && o3.d==30 && o4.d==60 && o5.d==45 &&
     val==45]
    -> t = root;
    -> t = t.right;
    -> t = t.left;
    -> t = t.right;
    -> t = t.left;
    -> res = t;
  }

  example {
    [root==o1 && o1.right==o2 && o2.right==o3 &&
     o1.d==4 && o2.d==5 && o3.d==6 && 
     val==6]
    -> t = root;
    -> t = t.right;
    -> t = t.right;
    -> res = t;
  }
  
  example {
    [root==o1 && o1.left==o2 && o2.left==o3 &&
     o1.d==4 && o2.d==3 && o3.d==2 && 
     val==2] 
	-> t = root;
    -> t = t.left;
    -> t = t.left;
    -> res = t;
  }
}
