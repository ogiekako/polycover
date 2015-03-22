package ui;

import java.util.Deque;
import java.util.LinkedList;

import main.Poly;

public class Model {

  Deque<Poly> polyHistory = new LinkedList<Poly>();

  private Poly poly;

  public void flip(int x, int y) {
    polyHistory.addLast(poly.clone());
    poly.flip(x, y);
  }

  public boolean get(int x, int y) {
    return poly.get(x, y);
  }

  public int getHeight() {
    return poly.getHeight();
  }

  public int getWidth() {
    return poly.getWidth();
  }

  Model(Poly poly) {
    this.poly = poly;
  }

  public void setPoly(Poly poly) {
    polyHistory.addLast(this.poly);
    if (polyHistory.size() > 100) {
      polyHistory.removeFirst();
    }
    this.poly = poly;
  }

  public String toString() {
    return poly.toString();
  }

  public boolean isNull() {
    return poly == null;
  }

  public Poly getPoly() {
    return poly;
  }

  public void undo() {
    if (!polyHistory.isEmpty()) {
      this.poly = polyHistory.pollLast();
    }
  }
}
