package main;

import java.util.HashSet;
import java.util.Set;

class Node {

  int myId;
  int candId;
  Cell candMoveVec;
  Set<Node> possiblePairs = new HashSet<Node>(64);
  // bit mask represents the cells in problem this state covers.
  long mask;
  // true if this state will never be a part of any solution.
  boolean hopeless;

  public Node(int candId, Cell candMoveVec) {
    super();
    this.candId = candId;
    this.candMoveVec = candMoveVec;
  }

  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + candMoveVec.x;
    result = prime * result + candMoveVec.y;
    result = prime * result + candId;
    return result;
  }

  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Node other = (Node) obj;
    if (candMoveVec.x != other.candMoveVec.x) {
      return false;
    }
    if (candMoveVec.y != other.candMoveVec.y) {
      return false;
    }
    if (candId != other.candId) {
      return false;
    }
    return true;
  }

  public String toString() {
    return "State [candMoveVec=" + candMoveVec +
           ", possiblePairs.size()=" + possiblePairs.size() +
           ", candId=" + candId +
           ", mask=" + mask +
           ", hopeless=" + hopeless +
           "]";
  }
}
