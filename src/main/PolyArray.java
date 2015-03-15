package main;

import java.util.Arrays;
import java.util.Scanner;

public class PolyArray implements Poly {

  boolean[][] array;

  public PolyArray(boolean[][] a) {
    assert a.length > 0 && a[0].length > 0;
    array = new boolean[a.length][a[0].length];
    for (int i = 0; i < a.length; i++) {
      for (int j = 0; j < a[0].length; j++) {
        array[i][j] = a[i][j];
      }
    }
  }

  @Override
  public boolean get(int x, int y) {
    if (!(0 <= x && x < array.length && 0 <= y && y < array[0].length)) {
      throw new IllegalArgumentException(x + " " + y);
    }
    return array[x][y];
  }

  @Override
  public void flip(int x, int y) {
    array[x][y] = !array[x][y];
  }

  @Override
  public int getHeight() {
    return array.length;
  }

  @Override
  public int getWidth() {
    return array[0].length;
  }

  public static PolyArray load(Scanner sc) {
    int h = sc.nextInt(), w = sc.nextInt();
    boolean[][] array = new boolean[h][w];
    for (int i = 0; i < h; i++) {
      String s = sc.next();
      for (int j = 0; j < w; j++) {
        array[i][j] = s.charAt(j) == '#';
      }
    }
    return new PolyArray(array);
  }

  @Override
  public Poly rot90() {
    int h = getHeight(), w = getWidth();
    boolean[][] bs = new boolean[w][h];
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        bs[w - 1 - j][i] = array[i][j];
      }
    }
    return new PolyArray(bs);
  }

  @Override
  public Poly flip() {
    int h = getHeight(), w = getWidth();
    boolean[][] bs = new boolean[h][w];
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        bs[i][w - 1 - j] = array[i][j];
      }
    }
    return new PolyArray(bs);
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
    return new PolyArray(res);
  }

  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.deepHashCode(array);
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
    PolyArray other = (PolyArray) obj;
    return Arrays.deepEquals(array, other.array);
  }

  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append(getHeight() + " " + getWidth() + "\n");
    for (int i = 0; i < getHeight(); i++) {
      for (int j = 0; j < getWidth(); j++) {
        b.append(array[i][j] ? '#' : '.');
      }
      b.append('\n');
    }
    return b.toString();
  }

  @Override
  public Poly clone() {
    return new PolyArray(array);
  }
}
