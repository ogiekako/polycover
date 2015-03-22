package ai;

import main.Covering;
import main.Judge;
import main.NoCellException;
import main.Poly;
import main.Stopwatch;

public enum Evaluator {
  DepthAndNumSolutionsIn23 {
    @Override
    Result eval(Poly prob, Poly cand, Stopwatch latency) {
      long off = (long) 1e6;
      DepthAndCovering res2 = computeDepthAndCovering(prob, cand, 2, true, latency);
      if (res2.depth >= INF) {
        DepthAndCovering res3 = computeDepthAndCovering(prob, cand, 3, true, latency);
        if (res3.depth >= INF) {
          return new Result(null, INF);
        }
        return new Result(res3.covering,
                          off * off + res3.depth * off + off - res3.numWayOfCovering);
      } else {
        return new Result(res2.covering, res2.depth * off + off - res2.numWayOfCovering);
      }
    }
  },
  DepthAndNumSolutionsIn2 {
    @Override
    Result eval(Poly prob, Poly cand, Stopwatch latency) {
      long off = (long) 1e6;
      DepthAndCovering res2 = computeDepthAndCovering(prob, cand, 2, true, latency);
      if (res2.depth == INF) {
        return new Result(null, INF);
      } else {
        return new Result(res2.covering, res2.depth * off + off - res2.numWayOfCovering);
      }
    }
  },
  DepthIn2 {
    @Override
    Result eval(Poly prob, Poly cand, Stopwatch latency) {
      DepthAndCovering res = computeDepthAndCovering(prob, cand, 2, false, latency);
      if (res.depth >= INF) {
        return new Result(null, INF);
      } else {
        return new Result(res.covering, res.depth);
      }
    }
  }, NumSolutionsIn2 {
    @Override
    Result eval(Poly prob, Poly cand, Stopwatch latency) {
      Judge judge = Judge.newBuilder(prob, cand)
          .setMaxNumCands(2)
          .setMinNumCands(2)
          .setAlsoNumSolutions()
          .setLatencyMetric(latency)
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

  abstract Result eval(Poly prob, Poly cand, Stopwatch latency);

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
      if (objective < INF && covering == null) {
        throw new AssertionError();
      }
      if (objective > INF) {
        throw new AssertionError();
      }
    }
  }

  public static final long INF = (long) 1e18;

  static class DepthAndCovering {

    long depth;
    Covering covering;
    long numWayOfCovering;

    public DepthAndCovering(Covering covering, long depth, long numWayOfCovering) {
      this.covering = covering;
      this.depth = depth;
      this.numWayOfCovering = numWayOfCovering;
    }
  }

  private static DepthAndCovering computeDepthAndCovering(Poly prob, Poly cand, int numCand,
                                                          boolean numSolutions, Stopwatch latency) {
    int n = Math.max(cand.getHeight(), cand.getWidth()) / 2 + 1;
    int dep = 0;
    Judge.Result result = null;
    for (int b = Integer.highestOneBit(n); b > 0; b >>= 1) {
      int nd = dep + b;
      Judge.Builder builder = Judge.newBuilder(prob, cand)
          .setMinNumCands(numCand)
          .setMaxNumCands(numCand)
          .setLatencyMetric(latency)
          .setEnabledCandDepth(nd);
      Judge.Result jRes;
      try {
        jRes = builder.build().judge();
      } catch (NoCellException e) {
        throw new AssertionError(e);
      }
      if (jRes.covering == null) {// cannot cover
        dep += b;
      } else {
        result = jRes;
      }
    }
    if (!numSolutions) {
      return new DepthAndCovering(result == null ? null : result.covering,
                                  dep >= n ? INF : dep,
                                  result == null ? 0 : result.numWayOfCovering);
    }
    Judge.Builder builder = Judge.newBuilder(prob, cand)
        .setMinNumCands(numCand)
        .setMaxNumCands(numCand)
        .setEnabledCandDepth(dep + 1)
        .setAlsoNumSolutions();
    try {
      result = builder.build().judge();
      return new DepthAndCovering(result.covering, dep >= n ? INF : dep, result.numWayOfCovering);
    } catch (NoCellException e) {
      throw new AssertionError(e);
    }
  }
}
