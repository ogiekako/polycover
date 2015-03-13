package main;

public class Cell {

  public final int x, y;

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
    final int prime = 31;
    int result = 1;
    result = prime * result + x;
    result = prime * result + y;
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
    Cell other = (Cell) obj;
    if (x != other.x) {
      return false;
    }
    if (y != other.y) {
      return false;
    }
    return true;
  }

  public String toString() {
    return "(" + x + "," + y + ")";
  }

}
