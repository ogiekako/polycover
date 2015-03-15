package ui;

import main.Poly;
import main.PolyArray;

public class Model implements AbstModel, Poly {

  Poly poly;

  public void flip(int x, int y) {
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

  public Poly flip() {
    throw new UnsupportedOperationException();
  }

  public Poly rot90() {
    throw new UnsupportedOperationException();
  }

  public Poly trim() {
    throw new UnsupportedOperationException();
  }

  public Poly clone() {throw new UnsupportedOperationException(); }

  Model(Poly poly) {
    this.poly = poly;
  }

  public void setPoly(PolyArray poly) {
    this.poly = poly;
  }

  public boolean isNull() {
    return poly == null;
  }
}
