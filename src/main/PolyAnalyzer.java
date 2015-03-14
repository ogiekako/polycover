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
    loop:
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        if (cell[i][j]) {
          dfs(cell, i, j);
          break loop;
        }
      }
    }
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        if (cell[i][j]) {
          return false;
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

  private void dfs(boolean[][] cell, int x, int y) {
    int h = cell.length, w = cell[0].length;
    assert cell[x][y];
    cell[x][y] = false;
    for (int d = 0; d < 4; d++) {
      int nx = x + dx[d], ny = y + dy[d];
      if (0 <= nx && nx < h && 0 <= ny && ny < w && cell[nx][ny]) {
        dfs(cell, nx, ny);
      }
    }
  }
}
