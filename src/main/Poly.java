package main;

import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

public class Poly {

  private final int h, w;
  private Bits[] rows;
  private String filePath;
  private int hash;

  public Poly(boolean[][] a) {
    assert a.length > 0 && a[0].length > 0;
    h = a.length;
    w = a[0].length;
    rows = new Bits[h];
    for (int i = 0; i < h; i++) {
      rows[i] = new Bits();
      for (int j = 0; j < w; j++) {
        if (a[i][j]) {
          rows[i].set(j);
        }
      }
    }
  }

  public Poly(Bits[] rows, int width) {
    h = rows.length;
    w = width;
    this.rows = new Bits[rows.length];
    for (int i = 0; i < rows.length; i++) {
      this.rows[i] = rows[i].clone();
    }
  }

  public Poly(Collection<Cell> cells) {
    this(toArray(cells));
  }

  public Poly(int h, int w) {
    this.h = h;
    this.w = w;
    rows = new Bits[h];
    for (int i = 0; i < h; i++) {
      rows[i] = new Bits();
    }
  }

  private static boolean[][] toArray(Collection<Cell> cells) {
    int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
    int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
    for (Cell c : cells) {
      minX = Math.min(minX, c.x);
      minY = Math.min(minY, c.y);
      maxX = Math.max(maxX, c.x);
      maxY = Math.max(maxY, c.y);
    }
    boolean[][] as = new boolean[maxX - minX + 1][maxY - minY + 1];
    for (Cell c : cells) {
      as[c.x - minX][c.y - minY] = true;
    }
    return as;
  }

  public boolean get(int x, int y) {
    if (!(0 <= x && x < getHeight() && 0 <= y && y < getWidth())) {
      throw new IllegalArgumentException(x + " " + y);
    }
    return rows[x].get(y);
  }

  public void flip(int x, int y) {
    rows[x].flip(y);
  }

  public int getHeight() {
    return h;
  }

  public int getWidth() {
    return w;
  }

  public static Poly load(Scanner sc) {
    int h = sc.nextInt(), w = sc.nextInt();
    boolean[][] array = new boolean[h][w];
    for (int i = 0; i < h; i++) {
      String s = sc.next();
      for (int j = 0; j < w; j++) {
        array[i][j] = s.charAt(j) == '#';
      }
    }
    return new Poly(array);
  }


  public Poly rot90() {
    int h = getHeight(), w = getWidth();
    boolean[][] bs = new boolean[w][h];
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        bs[w - 1 - j][i] = get(i, j);
      }
    }
    return new Poly(bs);
  }


  public Poly flip() {
    int h = getHeight(), w = getWidth();
    boolean[][] bs = new boolean[h][w];
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        bs[i][w - 1 - j] = get(i, j);
      }
    }
    return new Poly(bs);
  }

  public Poly trim() {
    int x1 = 0, x2 = getHeight() - 1;
    int y1 = 0, y2 = getWidth() - 1;
    for (; ; ) {
      boolean ok = false;
      for (int i = 0; i < getWidth(); i++) {
        if (get(x1, i)) {
          ok = true;
          break;
        }
      }
      if (ok) {
        break;
      } else {
        x1++;
      }
    }
    for (; ; ) {
      boolean ok = false;
      for (int i = 0; i < getWidth(); i++) {
        if (get(x2, i)) {
          ok = true;
          break;
        }
      }
      if (ok) {
        break;
      } else {
        x2--;
      }
    }

    for (; ; ) {
      boolean ok = false;
      for (int i = 0; i < getHeight(); i++) {
        if (get(i, y1)) {
          ok = true;
          break;
        }
      }
      if (ok) {
        break;
      } else {
        y1++;
      }
    }
    for (; ; ) {
      boolean ok = false;
      for (int i = 0; i < getHeight(); i++) {
        if (get(i, y2)) {
          ok = true;
          break;
        }
      }
      if (ok) {
        break;
      } else {
        y2--;
      }
    }
    int nh = x2 - x1 + 1, nw = y2 - y1 + 1;
    boolean[][] res = new boolean[nh][nw];
    for (int i = 0; i < nh; i++) {
      for (int j = 0; j < nw; j++) {
        res[i][j] = get(i + x1, j + y1);
      }
    }
    return new Poly(res);
  }

  public int hashCode() {
    if (hash != 0) {
      return hash;
    }
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.deepHashCode(rows);
    return hash = result;
  }

  public boolean equals(Object obj) {
    Poly other = (Poly) obj;
    if (h != other.h || w != other.w) {
      return false;
    }
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        if (get(i, j) != other.get(i, j)) {
          return false;
        }
      }
    }
    return true;
  }

  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append(getHeight() + " " + getWidth() + "\n");
    for (int i = 0; i < getHeight(); i++) {
      for (int j = 0; j < getWidth(); j++) {
        b.append(get(i, j) ? '#' : '.');
      }
      b.append('\n');
    }
    return b.toString();
  }

  public Poly clone() {
    return new Poly(rows, w);
  }

  public String filePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  Bits[] toRows() {
    Bits[] res = new Bits[rows.length];
    for (int i = 0; i < res.length; i++) {
      res[i] = rows[i].clone();
    }
    return res;
  }
}
