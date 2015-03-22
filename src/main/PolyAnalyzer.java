package main;

public class PolyAnalyzer {

  private static final int[] dx = {1, 0, -1, 0};
  private static final int[] dy = {0, 1, 0, -1};
  private Poly poly;

  private PolyAnalyzer(Poly poly) {
    this.poly = poly;
  }


  public static PolyAnalyzer of(Poly poly) {
    return new PolyAnalyzer(poly);
  }

  /**
   * ポリオミノが穴開きでなければ,trueを返す.
   */
  public boolean hasNoHole() {
    int h = poly.getHeight(), w = poly.getWidth();
    boolean[][] cell = new boolean[h + 2][w + 2];
    for (int i = 0; i < h + 2; i++) {
      for (int j = 0; j < w + 2; j++) {
        cell[i][j] = true;
      }
    }
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        cell[i + 1][j + 1] = !poly.get(i, j);
      }
    }
    return connected(cell);
  }

  private boolean connected(boolean[][] cell) {
    int h = cell.length, w = cell[0].length;
    boolean done = false;
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        if (cell[i][j]) {
          if (done) {
            return false;
          }
          dfs(cell, i, j);
          done = true;
        }
      }
    }
    return true;
  }

  /**
   * ポリオミノが連結であるかどうかを返す.
   */
  public boolean isConnected() {
    int h = poly.getHeight(), w = poly.getWidth();
    boolean[][] cell = new boolean[h + 2][w + 2];
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        cell[i + 1][j + 1] = poly.get(i, j);
      }
    }
    return connected(cell);
  }

  private int dfs(boolean[][] cell, int x, int y) {
    int h = cell.length, w = cell[0].length;
    assert cell[x][y];
    cell[x][y] = false;
    int res = 1;
    for (int d = 0; d < 4; d++) {
      int nx = x + dx[d], ny = y + dy[d];
      if (0 <= nx && nx < h && 0 <= ny && ny < w && cell[nx][ny]) {
        res += dfs(cell, nx, ny);
      }
    }
    return res;
  }

  public boolean contains(Poly target) {
    Poly p = poly.trim();
    target = target.trim();
    for (int k = 0; k < 2; k++) {
      p = p.flip();
      for (int l = 0; l < 4; l++) {
        p = p.rot90();
        int h = p.getHeight();
        int w = p.getWidth();
        for (int oi = 0; oi < h; oi++) {
          for (int oj = 0; oj < w; oj++) {
            boolean yes = true;
            loop:
            for (int i = 0; i < target.getHeight(); i++) {
              for (int j = 0; j < target.getWidth(); j++) {
                if (target.get(i, j)) {
                  int ni = oi + i, nj = oj + j;
                  if (ni >= h || nj >= w || !p.get(oi + i, oj + j)) {
                    yes = false;
                    break loop;
                  }
                }
              }
            }
            if (yes) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  public int numComponents() {
    int h = poly.getHeight(), w = poly.getWidth();
    boolean[][] cell = new boolean[h][w];
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        cell[i][j] = poly.get(i, j);
      }
    }
    int res = 0;
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        if (cell[i][j]) {
          res++;
          dfs(cell, i, j);
        }
      }
    }
    return res;
  }

  public int minCompSize() {
    int h = poly.getHeight(), w = poly.getWidth();
    boolean[][] cell = new boolean[h][w];
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        cell[i][j] = poly.get(i, j);
      }
    }
    int res = Integer.MAX_VALUE;
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        if (cell[i][j]) {
          res = Math.min(res, dfs(cell, i, j));
        }
      }
    }
    return res;
  }
}
