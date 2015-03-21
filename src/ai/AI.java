package ai;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.Cell;
import main.Covering;
import main.Judge;
import main.Poly;
import main.PolyArray;
import main.ProgressMonitor;

/**
 * Monitorable and abortable AI.
 */
public class AI {

  public static final Logger logger = Logger.getLogger(AI.class.getName());

  private ProgressMonitor monitor = ProgressMonitor.DO_NOTHING;
  private AIOption opt = new AIOption();

  public boolean abort = false;

  public void abort() {
    abort = true;
  }

  private AI() {
  }

  Poly prob;
  State bestState;
  Set<Integer> seenStateHash = new HashSet<Integer>();
  TreeSet<State> stateQueue = new TreeSet<State>();

  public Result solve(Poly seed) {
    return solve(Collections.singletonList(seed));
  }

  public Result solve(List<Poly> seeds) {
    abort = false;
    try {
      computeBest(seeds);
    } catch (Throwable e) {
      e.printStackTrace();
      logger.severe(String.format("Error: %s", e));
    }
    monitor.setValue(0);
    return new Result(bestState.cand, bestState.objective);
  }

  private void computeBest(List<Poly> seeds) {
    for (Poly seed : seeds) {
      if (seed.getWidth() != seed.getHeight()) {
        throw new IllegalArgumentException("height and width must be the same");
      }
      State initState = eval(prob, seed);
      updateAndTellBestState(initState);
      push(initState);
    }
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
      if (cur.objective > bestState.objective) {
        updateAndTellBestState(cur);
      }
      if (cur.objective == Evaluator.INF) {
        return;
      }
      addNexts(cur);
    }
  }

  private void addNexts(State cur) {
    if (cur.covering == null) {
      throw new AssertionError();
    }
    List<Poly> possibleNextCands = getPossibleNextCands(cur);
    if (possibleNextCands == null) {
      return;
    }
    for (Poly nxtCand : possibleNextCands) {
      State state = eval(prob, nxtCand);
      push(state);
      if (state.objective == Evaluator.INF) {
        return;
      }
    }
  }

  private List<Poly> getPossibleNextCands(State cur) {
    Covering covering = cur.covering;
    int INF = 10000;
    int minX = INF, minY = INF, maxX = 0, maxY = 0;
    int h = covering.height();
    int w = covering.width();
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        if (Math.abs(covering.get(i, j)) == 1) {
          minX = Math.min(minX, i);
          minY = Math.min(minY, j);
          maxX = Math.max(maxX, i);
          maxY = Math.max(maxY, j);
        }
      }
    }
    // empty
    if (minX == INF) {
      return Collections.emptyList();
    }
    int n = cur.cand.getHeight();
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

    List<Poly> possibleNextCands = new ArrayList<Poly>();
    for (int i = 0; i < n; i++) {
      if (i < curOffsetX || minX + i >= covering.height()) {
        continue;
      }
      for (int j = 0; j < n; j++) {
        if (j < curOffsetY || minY + j >= covering.width()) {
          continue;
        }
        if (covering.get(minX + i, minY + j) < 0 || covering.get(minX + i, minY + j) > 1) {
          int i2 = n - 1 - i;
          int j2 = n - 1 - j;
          TreeSet<Cell> cs = new TreeSet<Cell>();
          Cell c = new Cell(i, j);
          if (!opt.validator.valid(cur.cand, c)) {
            continue;
          }
          Poly nxtCand = cur.cand.clone();
          cs.add(c);
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

          possibleNextCands.add(nxtCand);
        }
      }
    }
    return possibleNextCands;
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

  private State eval(Poly prob, Poly cand) {
    Evaluator.Result res = opt.objective.eval(prob, cand);
    return new State(cand, res.objective, res.covering);
  }

  private List<BestResultMonitor> bestResultMonitors = new ArrayList<BestResultMonitor>();

  private void updateAndTellBestState(State s) {
    bestState = s;
    tellBestResult(new Result(s.cand, s.objective));
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

    public Builder setOption(AIOption opt) {
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

  public static void main(String[] args) throws FileNotFoundException {
    Judge.logger.setLevel(Level.OFF);

    if (args.length == 0) {
      args = new String[2];
      args[0] = "problem/9/111100_000111_000001_000001.meh";
      args[1] = "ans/6/W.ans";
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
