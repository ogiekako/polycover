package main;

public class Cell {

  public final int x, y;
  int hash = 0;

  public Cell(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public Cell sub(Cell c) {
    return new Cell(x - c.x, y - c.y);
  }

  public Cell add(Cell c) {
    return new Cell(x + c.x, y + c.y);
  }

  public int hashCode() {
    if (hash != 0) {
      return hash;
    }
    final int prime = 31;
    hash = 1;
    hash = prime * hash + x;
    hash = prime * hash + y;
    return hash;
  }

  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    Cell other = (Cell) obj;
    return x == other.x && y == other.y;
  }

  public String toString() {
    return "(" + x + "," + y + ")";
  }

}
