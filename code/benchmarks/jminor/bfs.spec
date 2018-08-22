/** A node in a directed graph with at most two successors.
 */
type Node {
  left:Node
  right:Node  
  data:int
  
  next:Node // The BFS stack
  
  visited:boolean
}

/** A breadth-first search from the given node.
 */
bfs(start:Node) -> () {
  var current:Node
  var neighbor:Node
  var fringe:Node
  
  fringe = start;  
  
  while (fringe != null) {
    current = fringe;
    current.visited = true;
    fringe = fringe.next;
    
    neighbor = current.left;
    if (neighbor.visited == false) {
      neighbor.visited = true;
      neighbor.next = fringe;
      fringe = neighbor;
    }

    neighbor = current.right;
    if (neighbor.visited == false) {
      neighbor.visited = true;
      neighbor.next = fringe;
      fringe = neighbor;
    }
    
    neighbor = null;
  }

  example {
    [start==v1 && 
     v1.visited==false && v1.next==null &&
     v2.visited==false && v2.next==null &&
     v3.visited==false && v3.next==null &&
     v1.left==v2 && v1.right==v3 &&
     v2.left==v3 && v2.right==v1 &&
     v3.left==v1 && v3.right==v2
    ] -> ...
  }

  example {
    [start==v1 && 
     v1.visited==false && v1.next==null &&
     v2.visited==false && v2.next==null &&
     v3.visited==false && v3.next==null &&
     v1.left==v2 && v1.right==v2 &&
     v2.left==v3 && v2.right==v3 &&
     v3.left==v1 && v3.right==v1
    ] -> ...
  }

  example {
    [start==v1 && 
     v1.visited==false && v1.next==null &&
     v2.visited==false && v2.next==null &&
     v3.visited==false && v3.next==null &&
     v1.left==v1 && v1.right==v2 &&
     v2.left==v2 && v2.right==v3 &&
     v3.left==v3 && v3.right==v1
    ] -> ...
  }
  
  example {
    [start==v1 && 
     v1.visited==false && v1.next==null &&
     v2.visited==false && v2.next==null &&
     v3.visited==false && v3.next==null &&
     v4.visited==false && v4.next==null &&
     v1.left==v2 && v1.right==v1 &&
     v2.left==v3 && v2.right==v2 &&
     v3.left==v4 && v3.right==v3 &&
     v4.left==v1 && v4.right==v2
    ] -> ...
  }  

  test example {
    [start==v1 && 
     v1.visited==false && v1.next==null &&
     v2.visited==false && v2.next==null &&
     v3.visited==false && v3.next==null &&
     v4.visited==false && v4.next==null &&
     v1.left==v1 && v1.right==v2 &&
     v2.left==v2 && v2.right==v3 &&
     v3.left==v4 && v3.right==v2 &&
     v4.left==v2 && v4.right==v3
    ] -> ...
  }  
}
