package ai;

import main.Covering;
import main.Judge;
import main.NoCellException;
import main.Poly;

public enum Evaluator {
  DepthAndNumSolutionsIn2 {
    @Override
    Result eval(Poly prob, Poly cand) {
      int n = Math.max(cand.getHeight(), cand.getWidth()) / 2 + 1;
      int dep = 0;
      Judge.Result result = null;
      for (int b = Integer.highestOneBit(n); b > 0; b >>= 1) {
        int nd = dep + b;
        Judge judge = Judge.newBuilder(prob, cand)
            .setMinNumCands(2)
            .setMaxNumCands(2)
            .setEnabledCandDepth(nd)
            .setAlsoNumSolutions()
            .build();
        Judge.Result jRes;
        try {
          jRes = judge.judge();
        } catch (NoCellException e) {
          throw new AssertionError(e);
        }
        if (jRes.covering == null) {// cannot cover
          dep += b;
        } else {
          result = jRes;
        }
      }
      if (dep >= n) {
        return new Result(null, INF);
      } else {
        return new Result(result.covering, dep * (long) 1e9 + (long) 1e9 - result.numWayOfCovering);
      }
    }
  },
  DepthIn2 {
    @Override
    Result eval(Poly prob, Poly cand) {
      int n = Math.max(cand.getHeight(), cand.getWidth()) / 2 + 1;
      int dep = 0;
      Judge.Result result = null;
      for (int b = Integer.highestOneBit(n); b > 0; b >>= 1) {
        int nd = dep + b;
        Judge judge = Judge.newBuilder(prob, cand)
            .setMinNumCands(2)
            .setMaxNumCands(2)
            .setEnabledCandDepth(nd)
            .build();
        Judge.Result jRes;
        try {
          jRes = judge.judge();
        } catch (NoCellException e) {
          throw new AssertionError(e);
        }
        if (jRes.covering == null) {// cannot cover
          dep += b;
        } else {
          result = jRes;
        }
      }
      if (dep >= n) {
        return new Result(null, INF);
      } else {
        return new Result(result.covering, dep);
      }
    }
  }, NumSolutionsIn2 {
    @Override
    Result eval(Poly prob, Poly cand) {
      Judge judge = Judge.newBuilder(prob, cand)
          .setMaxNumCands(2)
          .setMinNumCands(2)
          .setAlsoNumSolutions()
          .build();
      Judge.Result jRes;
      try {
        jRes = judge.judge();
      } catch (NoCellException e) {
        throw new AssertionError(e);
      }
      return new Result(jRes.covering, INF - jRes.numWayOfCovering);
    }

    @Override
    public boolean negative() {
      return true;
    }
  },;

  abstract Result eval(Poly prob, Poly cand);

  public boolean negative() {
    return false;
  }

  public static class Result {

    Covering covering;
    // Larger is better.
    long objective;

    public Result(Covering covering, long objective) {
      this.covering = covering;
      this.objective = objective;
    }
  }

  public static final long INF = (long) 1e18;
}
