package ai;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import main.Cell;
import main.Judge;
import main.NoCellException;
import main.Poly;
import main.PolyAnalyzer;
import main.PolyArray;
import util.Debug;

public class AI {

  static boolean rotSym = false;
  static boolean revRotSym = true;
  static boolean allowHole = true;
  static boolean allowUnConnected = true;

  int INF = (int) 1e9;

  public static void main(String[] args) throws FileNotFoundException {
    Judge.logger.setLevel(Level.OFF);

    if (args.length == 0) {
      args = new String[2];
      args[0] = "problem/9/111100_000111_000001_000001.no";
      args[1] = "ans/hexomino/W.ans";
    }

    String probPath = args[0];
    String initCandPath = args[1];
    Poly prob = PolyArray.load(new Scanner(new File(probPath)));
    Poly cand = PolyArray.load(new Scanner(new File(initCandPath)));
    Poly res = new AI().solve(prob, cand);
    if (res == null) {
      System.out.println("Failed");
    } else {
      System.out.println(res);
    }
  }

  Poly prob;
  int bestDepth;
  int queueSize = 5;
  Set<Integer> seenStateHash = new HashSet<Integer>();
  TreeSet<State> stateQueue = new TreeSet<State>();
  int n;

  private Poly solve(Poly prob, Poly cand) {
    this.prob = prob;
    n = cand.getHeight();
    if (n != cand.getWidth()) {
      throw new IllegalArgumentException("height and width must be same");
    }
    State initResult = eval(prob, cand);
    bestDepth = initResult.maxAllowableDepth;
    push(initResult);
    System.err.println("initial: " + initResult.maxAllowableDepth);
    while (!stateQueue.isEmpty()) {
      State cur = stateQueue.pollFirst();
      if (cur.maxAllowableDepth > bestDepth) {
        bestDepth = cur.maxAllowableDepth;
        System.out.println(cur.cand);
      }
      Debug.debug("cur", cur.maxAllowableDepth);
      if (cur.maxAllowableDepth == INF) {
        return cur.cand;
      }
      addNexts(cur);
    }
    return null;
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
    if (maxX - minX + 1 != n) {
      throw new AssertionError();
    } else if (maxY - minY + 1 != n) {
      throw new AssertionError();
    }

    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        if (covering[minX + i][minY + j] > 1) {
          // rot sym
          int i2 = n - 1 - i;
          int j2 = n - 1 - j;
          TreeSet<Cell> cs = new TreeSet<Cell>();
          Poly nxtCand = cur.cand.clone();
          cs.add(new Cell(i, j));
          if (rotSym || revRotSym) {
            cs.add(new Cell(j, i2));
            cs.add(new Cell(i2, j2));
            cs.add(new Cell(j2, i));
          }
          if (revRotSym) {
            cs.add(new Cell(i, j2));
            cs.add(new Cell(j, j));
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
    if (!cand.trim().equals(cand)) return false;
    PolyAnalyzer analyzer = PolyAnalyzer.of(cand);
    if (!allowUnConnected && !analyzer.isConnected()) {
      return false;
    }
    if (!allowHole && !analyzer.hasNoHole()) {
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
    if (stateQueue.size() > queueSize) {
      // Remove worst state.
      stateQueue.pollLast();
    }
  }

  private void flip(Poly cand, Collection<Cell> cs) {
    for (Cell c : cs) {
      cand.flip(c.x, c.y);
    }
  }

  int globalStateId = 0;

  class State implements Comparable<State> {

    Poly cand;
    // INF if infinity (cand can be a solution).
    int maxAllowableDepth;
    int[][] covering;
    int hash;
    int id;

    public State(Poly cand, int maxAllowableDepth, int[][] covering) {
      this.cand = cand;
      this.maxAllowableDepth = maxAllowableDepth;
      this.covering = covering;
      this.id = globalStateId++;
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
        // newer (i.e. larger id) is better.
        return -(id - o.id);
      }
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
        throw new AssertionError();
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
}
