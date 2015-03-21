package ai;

import main.Cell;
import main.Poly;
import main.PolyAnalyzer;

public enum Validator {
  Connected {
    @Override
    boolean valid(Poly cand, Cell pos) {
      cand.flip(pos.x, pos.y);
      PolyAnalyzer an = PolyAnalyzer.of(cand);
      boolean res = an.isConnected() && an.hasNoHole();
      cand.flip(pos.x, pos.y);
      return res;
    }
  }, AllowHole {
    @Override
    boolean valid(Poly cand, Cell pos) {
      cand.flip(pos.x, pos.y);
      boolean res = PolyAnalyzer.of(cand).isConnected();
      cand.flip(pos.x, pos.y);
      return res;
    }
  }, AllowUnconnected {
    @Override
    boolean valid(Poly cand, Cell pos) {
      return true;
    }
  }, AllowHoleDisallowDiagonal {
    @Override
    boolean valid(Poly cand, Cell pos) {
      cand.flip(pos.x, pos.y);
      for (int i = 0; i < 2; i++) {
        for (int j = 0; j < 2; j++) {
          int x = pos.x - i;
          int y = pos.y - j;
          boolean bad = false;
          bad |= get(cand, x, y) && get(cand, x + 1, y + 1)
                 && !get(cand, x, y + 1) && !get(cand, x + 1, y);
          bad |= !get(cand, x, y) && !get(cand, x + 1, y + 1)
                 && get(cand, x, y + 1) && get(cand, x + 1, y);
          if (bad) {
            cand.flip(pos.x, pos.y);
            return false;
          }
        }
      }
      boolean res = PolyAnalyzer.of(cand).isConnected();
      cand.flip(pos.x, pos.y);
      return res;
    }

    private boolean get(Poly cand, int x, int y) {
      if (0 <= x && x < cand.getHeight() && 0 <= y && y < cand.getWidth()) {
        return cand.get(x, y);
      }
      return false;
    }

  };

  abstract boolean valid(Poly cand, Cell pos);
}
