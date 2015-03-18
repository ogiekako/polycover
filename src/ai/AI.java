package ai;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.Cell;
import main.Judge;
import main.NoCellException;
import main.Poly;
import main.PolyAnalyzer;
import main.PolyArray;
import main.ProgressMonitor;

/**
 * Monitorable and abortable AI.
 */
public class AI {

  private static final Logger logger = Logger.getLogger(AI.class.getName());

  public static final int INF = (int) 1e9;
  private ProgressMonitor monitor = ProgressMonitor.DO_NOTHING;
  private Option opt = new Option();

  public boolean abort = false;

  public void abort() {
    abort = true;
  }

  public static class Result {

    public final Poly convertedCand;
    public final int maxAllowableDepth;

    public Result(Poly convertedCand, int maxAllowableDepth) {
      this.convertedCand = convertedCand;
      this.maxAllowableDepth = maxAllowableDepth;
    }
  }

  public static class Option {

    public int maxIter = 100;
    public boolean rotSym = true;
    public boolean revRotSym = false;
    public boolean allowHole = true;
    public boolean allowUnconnected = false;
    public int queueSize = 5;

    @Override
    public String toString() {
      return "Option{" +
             "allowHole=" + allowHole +
             ", maxIter=" + maxIter +
             ", rotSym=" + rotSym +
             ", revRotSym=" + revRotSym +
             ", allowUnconnected=" + allowUnconnected +
             ", queueSize=" + queueSize +
             '}';
    }
  }

  private AI() {
  }

  Poly prob;
  State bestState;
  Set<Integer> seenStateHash = new HashSet<Integer>();
  TreeSet<State> stateQueue = new TreeSet<State>();
  int n;

  public Result solve(Poly seed) {
    abort = false;
    try {
      computeBest(seed);
    } catch (Throwable e) {
      e.printStackTrace();
      logger.severe(String.format("Error: %s", e));
    }
    monitor.setValue(0);
    return new Result(bestState.cand, bestState.maxAllowableDepth);
  }

  private void computeBest(Poly seed) {
    n = seed.getHeight();
    if (n != seed.getWidth()) {
      throw new IllegalArgumentException("height and width must be the same");
    }
    State initState = eval(prob, seed);
    updateAndTellBestState(initState);
    push(initState);
    int numIter = 0;
    while (!stateQueue.isEmpty()) {
      if (abort) {
        return;
      }
      State cur = stateQueue.pollFirst();
      if (numIter++ >= opt.maxIter) {
        return;
      }
      monitor.setValue((int) Math.round(numIter * 100.0 / opt.maxIter));
      logger.info("numIter: " + numIter);
      logger.info("state size: " + stateQueue.size());
      if (cur.maxAllowableDepth > bestState.maxAllowableDepth) {
        updateAndTellBestState(cur);
      }
      if (cur.maxAllowableDepth == INF) {
        return;
      }
      addNexts(cur);
    }
  }

  private void addNexts(State cur) {
    int[][] covering = cur.covering;
    int minX = INF, minY = INF, maxX = 0, maxY = 0;
    int h = covering.length;
    int w = covering[0].length;
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        if (Math.abs(covering[i][j]) == 1) {
          minX = Math.min(minX, i);
          minY = Math.min(minY, j);
          maxX = Math.max(maxX, i);
          maxY = Math.max(maxY, j);
        }
      }
    }
    // empty
    if (minX == INF) {
      return;
    }
    int curOffsetX = INF, curOffsetY = INF;
    loop:
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        if (cur.cand.get(i, j)) {
          curOffsetX = i;
          break loop;
        }
      }
    }
    loop:
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        if (cur.cand.get(j, i)) {
          curOffsetY = i;
          break loop;
        }
      }
    }
    minX -= curOffsetX;
    minY -= curOffsetY;

    for (int i = 0; i < n; i++) {
      if (i < curOffsetX || minX + i >= covering.length) {
        continue;
      }
      for (int j = 0; j < n; j++) {
        if (j < curOffsetY || minY + j >= covering[0].length) {
          continue;
        }
        if (covering[minX + i][minY + j] < 0 || covering[minX + i][minY + j] > 1) {
          int i2 = n - 1 - i;
          int j2 = n - 1 - j;
          TreeSet<Cell> cs = new TreeSet<Cell>();
          Poly nxtCand = cur.cand.clone();
          cs.add(new Cell(i, j));
          if (opt.rotSym || opt.revRotSym) {
            cs.add(new Cell(j, i2));
            cs.add(new Cell(i2, j2));
            cs.add(new Cell(j2, i));
          }
          if (opt.revRotSym) {
            cs.add(new Cell(i, j2));
            cs.add(new Cell(j, i));
            cs.add(new Cell(i2, j));
            cs.add(new Cell(j2, i2));
          }
          flip(nxtCand, cs);

          if (validCand(nxtCand)) {
            State state = eval(prob, nxtCand);
            push(state);
            if (state.maxAllowableDepth == INF) {
              return;
            }
          }
        }
      }
    }
  }

  private boolean validCand(Poly cand) {
    PolyAnalyzer analyzer = PolyAnalyzer.of(cand);
    if (!opt.allowUnconnected && !analyzer.isConnected()) {
      return false;
    }
    if (!opt.allowHole && !analyzer.hasNoHole()) {
      return false;
    }
    return true;
  }

  private void push(State state) {
    if (seenStateHash.contains(state.hashCode())) {
      return;
    }
    seenStateHash.add(state.hashCode());
    stateQueue.add(state);
    if (stateQueue.size() > opt.queueSize) {
      // Remove worst state.
      stateQueue.pollLast();
    }
  }

  private void flip(Poly cand, Collection<Cell> cs) {
    for (Cell c : cs) {
      cand.flip(c.x, c.y);
    }
  }

  Random rnd = new Random(1102840128L);

  class State implements Comparable<State> {

    Poly cand;
    // INF if infinity (cand can be a solution).
    int maxAllowableDepth;
    int[][] covering;
    int hash;
    long id;

    public State(Poly cand, int maxAllowableDepth, int[][] covering) {
      this.cand = cand;
      this.maxAllowableDepth = maxAllowableDepth;
      this.covering = covering;
      this.id = rnd.nextLong();
    }

    @Override
    public int hashCode() {
      if (hash != 0) {
        return hash;
      }
      return hash = cand.hashCode();
    }

    @Override
    public int compareTo(State o) {
      if (maxAllowableDepth != o.maxAllowableDepth) {
        // deeper is better.
        return -(maxAllowableDepth - o.maxAllowableDepth);
      } else {
        // tie is broken randomly
        return Long.compare(id, o.id);
      }
    }

    @Override
    public String toString() {
      return "State{" +
             "cand=" + cand +
             ", maxAllowableDepth=" + maxAllowableDepth +
             ", covering=" + Arrays.toString(covering) +
             ", hash=" + hash +
             ", id=" + id +
             '}';
    }
  }

  private State eval(Poly prob, Poly cand) {
    int n = Math.max(cand.getHeight(), cand.getWidth()) / 2 + 1;
    int dep = 0;
    int[][] covering = null;
    for (int b = Integer.highestOneBit(n); b > 0; b >>= 1) {
      int nd = dep + b;
      Judge judge = Judge.newBuilder(prob, cand)
          .setMinNumCands(2)
          .setMaxNumCands(2)
          .setEnabledCandDepth(nd)
          .build();
      int[][] res;
      try {
        res = judge.judge();
      } catch (NoCellException e) {
        throw new AssertionError(e);
      }
      if (res == null) {
        dep += b;
      } else {
        covering = res;
      }
    }
    if (dep >= n) {
      return new State(cand, INF, covering);
    } else {
      return new State(cand, dep, covering);
    }
  }

  private List<BestResultMonitor> bestResultMonitors = new ArrayList<BestResultMonitor>();

  private void updateAndTellBestState(State s) {
    bestState = s;
    tellBestResult(new Result(s.cand, s.maxAllowableDepth));
  }
  private void tellBestResult(Result result) {
    for (BestResultMonitor m : bestResultMonitors) {
      m.update(result);
    }
  }

  public static class Builder {

    private AI ai = new AI();

    private Builder(Poly problem) {
      ai.prob = problem;
    }

    public AI build() {
      return ai;
    }

    public Builder setOption(Option opt) {
      ai.opt = opt;
      return this;
    }

    public Builder setMonitor(ProgressMonitor monitor) {
      ai.monitor = monitor;
      return this;
    }

    public Builder addBestResultMonitor(BestResultMonitor m) {
      ai.bestResultMonitors.add(m);
      return this;
    }
  }

  public static AI.Builder builder(Poly problem) {
    return new AI.Builder(problem);
  }

  public static interface BestResultMonitor {

    void update(Result result);
  }

  public static void main(String[] args) throws FileNotFoundException {
    Judge.logger.setLevel(Level.OFF);

    if (args.length == 0) {
      args = new String[2];
      args[0] = "problem/9/111100_000111_000001_000001.meh";
      args[1] = "ans/hexomino/W.ans";
    }

    String probPath = args[0];
    String initCandPath = args[1];
    Poly prob = PolyArray.load(new Scanner(new File(probPath)));
    Poly cand = PolyArray.load(new Scanner(new File(initCandPath)));
    Poly res = AI.builder(prob).build().solve(cand).convertedCand;
    if (res == null) {
      System.out.println("Failed");
    } else {
      System.out.println(res);
    }
  }
}
