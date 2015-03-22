package main;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Scanner;

public class Poly {

  private int h, w;
  private BitSet[] rows;
  private String filePath;
  private int hash;

  public Poly(boolean[][] a) {
    assert a.length > 0 && a[0].length > 0;
    h = a.length;
    w = a[0].length;
    rows = new BitSet[h];
    for (int i = 0; i < h; i++) {
      rows[i] = new BitSet();
      for (int j = 0; j < w; j++) {
        if (a[i][j]) {
          rows[i].set(j);
        }
      }
    }
  }

  public Poly(BitSet[] rows) {
    this.rows = new BitSet[rows.length];
    for (int i = 0; i < rows.length; i++) {
      this.rows[i] = (BitSet) rows[i].clone();
    }
  }

  @Override
  public boolean get(int x, int y) {
    if (!(0 <= x && x < h && 0 <= y && y < w)) {
      throw new IllegalArgumentException(x + " " + y);
    }
    return rows[x].get(y);
  }

  @Override
  public void flip(int x, int y) {
    rows[x].flip(y);
  }

  @Override
  public int getHeight() {
    return h;
  }

  @Override
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

  @Override
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

  @Override
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

  @Override
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
    return Arrays.deepEquals(rows, other.rows);
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

  @Override
  public Poly clone() {
    return new Poly(rows);
  }

  @Override
  public String filePath() {
    return filePath;
  }

  @Override
  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }
}
