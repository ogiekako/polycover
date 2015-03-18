package ui;

import main.Poly;

public class Model implements Poly {

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
    return poly.rot90();
  }

  public Poly rot90() {
    return poly.rot90();
  }

  public Poly trim() {
    return poly.trim();
  }

  public Poly clone() {
    return poly.clone();
  }

  Model(Poly poly) {
    this.poly = poly;
  }

  public void setPoly(Poly poly) {
    this.poly = poly;
  }

  @Override
  public String toString() {
    return poly.toString();
  }

  public boolean isNull() {
    return poly == null;
  }
}
