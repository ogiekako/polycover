package main;

public interface Poly {

  /**
   * (x,y)でのセルの有無を返す. 0<=x<size().x() , 0<=y<size().y() でなければならない.
   */
  boolean get(int x, int y);

  /**
   * (x,y)のセルの有無を反転する.
   */
  void flip(int x, int y);

  int getHeight();

  int getWidth();

  /**
   * 90度回転.方向は問わない.
   */
  Poly rot90();

  /**
   * とにかく裏返しにすれば良い
   */
  Poly flip();

  /**
   * 周辺の空白を取り除いたpoly を返す.
   */
  Poly trim();

  Poly clone();
}
