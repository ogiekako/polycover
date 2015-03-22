package ai;

import java.util.Collection;

import main.Cell;
import main.Poly;
import main.PolyAnalyzer;

public enum Validator {
  Inner_9Comp_NoDiag_ConcatMinComp {
    @Override
    Decision validate(Poly cand, Collection<Cell> cells) {
      PolyAnalyzer prevAn = PolyAnalyzer.of(cand);
      cand = cand.clone();
      Cell p = null;
      for (Cell pos : cells) {
        p = pos;
        cand.flip(pos.x, pos.y);
      }

      PolyAnalyzer an = PolyAnalyzer.of(cand);
      int numComp = an.numComponents();
      if (9 < numComp) {
        return Decision.NG;
      }
      if (numComp > 1) {
        int prevNumComp = prevAn.numComponents();
        if (1 < prevNumComp && prevNumComp < numComp) {
          return Decision.NG;
        }
        if (prevNumComp >= numComp && an.minCompSize() <= prevAn.minCompSize()) {
          return Decision.NG;
        }
      }

      if (innerOrNeighbour(cand, p)) {
        return Decision.NG;
      }

      for (int i = 0; i < 2; i++) {
        for (int j = 0; j < 2; j++) {
          int x = p.x - i;
          int y = p.y - j;
          boolean bad = false;
          bad |= get(cand, x, y) && get(cand, x + 1, y + 1)
                 && !get(cand, x, y + 1) && !get(cand, x + 1, y);
          bad |= !get(cand, x, y) && !get(cand, x + 1, y + 1)
                 && get(cand, x, y + 1) && get(cand, x + 1, y);
          if (bad) {
            return Decision.NG;
          }
        }
      }
      return numComp == 1 ? Decision.OK : Decision.Intermediate;
    }

    private boolean innerOrNeighbour(Poly cand, Cell p) {
      boolean inner = false;
      if (!cand.get(p.x, p.y)) {
        inner = true;
      } else {
        inner |= get(cand, p.x, p.y - 1);
        inner |= get(cand, p.x - 1, p.y);
        inner |= get(cand, p.x, p.y + 1);
        inner |= get(cand, p.x + 1, p.y);
      }
      if (!inner) {
        int cnt = 0;
        for (int x = p.x - 1; x >= 0; x--) {
          if (cand.get(x, p.y)) {
            cnt++;
            break;
          }
        }
        for (int x = p.x + 1; x < cand.getHeight(); x++) {
          if (cand.get(x, p.y)) {
            cnt++;
            break;
          }
        }
        if (cnt == 2) {
          inner = true;
        }
      }
      if (!inner) {
        int cnt = 0;
        for (int y = p.y - 1; y >= 0; y--) {
          if (cand.get(p.x, y)) {
            cnt++;
            break;
          }
        }
        for (int y = p.x + 1; y < cand.getWidth(); y++) {
          if (cand.get(p.x, y)) {
            cnt++;
            break;
          }
        }
        if (cnt == 2) {
          inner = true;
        }
      }
      if (!inner) {
        return true;
      }
      return false;
    }
  },
  NoSeparate {
    @Override
    Decision validate(Poly cand, Collection<Cell> cs) {
      int prevNumComp = PolyAnalyzer.of(cand).numComponents();
      cand = cand.clone();
      for (Cell pos : cs) {
        cand.flip(pos.x, pos.y);
      }
      PolyAnalyzer an = PolyAnalyzer.of(cand);
      if (prevNumComp < an.numComponents()) {
        return Decision.NG;
      }
      boolean res = an.hasNoHole();
      return res ? Decision.OK : Decision.NG;
    }
  }, AllowHole {
    @Override
    Decision validate(Poly cand, Collection<Cell> cs) {
      cand = cand.clone();
      for (Cell pos : cs) {
        cand.flip(pos.x, pos.y);
      }
      boolean res = PolyAnalyzer.of(cand).isConnected();
      return res ? Decision.OK : Decision.NG;
    }
  }, AllowUnconnected {
    @Override
    Decision validate(Poly cand, Collection<Cell> pos) {
      return Decision.OK;
    }
  }, AllowHoleDisallowDiagonal {
    @Override
    Decision validate(Poly cand, Collection<Cell> pos) {
      cand = cand.clone();
      Cell c = null;
      for (Cell d : pos) {
        cand.flip(d.x, d.y);
        c = d;
      }
      for (int i = 0; i < 2; i++) {
        for (int j = 0; j < 2; j++) {
          int x = c.x - i;
          int y = c.y - j;
          boolean bad = false;
          bad |= get(cand, x, y) && get(cand, x + 1, y + 1)
                 && !get(cand, x, y + 1) && !get(cand, x + 1, y);
          bad |= !get(cand, x, y) && !get(cand, x + 1, y + 1)
                 && get(cand, x, y + 1) && get(cand, x + 1, y);
          if (bad) {
            return Decision.NG;
          }
        }
      }
      return PolyAnalyzer.of(cand).isConnected() ? Decision.OK : Decision.NG;
    }

  };

  abstract Decision validate(Poly cand, Collection<Cell> cells);

  private static boolean get(Poly cand, int x, int y) {
    if (0 <= x && x < cand.getHeight() && 0 <= y && y < cand.getWidth()) {
      return cand.get(x, y);
    }
    return false;
  }

  public static enum Decision {
    OK,
    NG,
    Intermediate,
  }
}
