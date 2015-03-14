package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import ui.AbstProgressMonitor;
import util.Debug;

public class Judge {

  static Logger logger = Logger.getLogger(Judge.class.getName());
  // the problem, or the polyomino to be covered.
  Poly problem;
  // the candidate, or the polyomino whose multiple copies are used to cover the target.
  Poly candidate;
  // number of candidates that can be used.
  int numCandidates = (int) 1e9;
  int enabledCandDepth = (int) 1e9;
  AbstProgressMonitor monitor = new AbstProgressMonitor() {
    @Override
    public void setValue(int n) {
      // Do nothing.
    }
  };
  Stopwatch latencyMetric = Stopwatch.DO_NOTHING;

  int numCellsInProblem;

  State[] states;
  long[] masks;
  Map<Long, List<State>> maskToState;

  Judge(Poly problem, Poly candidate) {
    this.problem = problem;
    this.candidate = candidate;
  }

  public static Builder newBuilder(Poly problem, Poly candidate) {
    return new Builder(problem, candidate);
  }

  public static class Builder {

    private Judge judge;

    private Builder(Poly problem, Poly candidate) {
      judge = new Judge(problem, candidate);
    }

    public Builder setMonitor(AbstProgressMonitor monitor) {
      judge.monitor = monitor;
      return this;
    }

    public Builder setNumCandidates(int numCandidates) {
      judge.numCandidates = numCandidates;
      return this;
    }

    public Builder setEnabledCandDepth(int enabledCandDepth) {
      judge.enabledCandDepth = enabledCandDepth;
      return this;
    }

    public Builder setLatencyMetric(Stopwatch latencyMetric) {
      judge.latencyMetric = latencyMetric;
      return this;
    }

    public Judge build() {
      return judge;
    }
  }

  /**
   * problem が candidate で,重ならずに覆えるかどうかを判定する. 覆えない場合は,null を返す. 覆える場合は,覆った状態を表すint[][] を返す. 0は空白,1~
   * が candidate のある位置を表し, -1 ~ が, problem と candidate が重なっていることを表す. numCandidates は,使用する candidate
   * の最大枚数を示す. enabledCandDepth,有効なセルのバウンディングボックスからの距離を示す.例えば,1なら,周辺のセルしかみない.
   * また,その内側にはブロックがおいてあるかのように,入れない.
   */
  public int[][] judge() throws NoCellException {
    // TODO: extract method.
    numCellsInProblem = 0;
    for (int i = 0; i < problem.getHeight(); i++) {
      for (int j = 0; j < problem.getWidth(); j++) {
        if (problem.get(i, j)) {
          numCellsInProblem++;
        }
      }
    }
    if (numCellsInProblem > 64) {
      throw new IllegalArgumentException("Number of cells in the problem must be at most 64.");
    }
    Cell[] cellsInProblem = new Cell[numCellsInProblem];
    for (int i = 0, k = 0; i < problem.getHeight(); i++) {
      for (int j = 0; j < problem.getWidth(); j++) {
        if (problem.get(i, j)) {
          cellsInProblem[k++] = new Cell(i, j);
        }
      }
    }

    if (candidate == null) {
      throw new NoCellException();
    }

    if (monitor != null) {
      monitor.setValue(0);
    }

    candidate = candidate.trim();
    HashSet<Poly> candSet = new HashSet<Poly>();
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 4; j++) {
        candSet.add(candidate);
        candidate = candidate.rot90();
      }
      candidate = candidate.flip();
    }
    Poly[] candidates = candSet.toArray(new Poly[candSet.size()]);

    int numCandPattern = candidates.length;
    logger.info(Debug.toString("numCandPattern", numCandPattern));

    int numEnabledCandCells = getCellsOnPeripheral(candidate, enabledCandDepth).size();
    if (numEnabledCandCells == 0) {
      throw new NoCellException();
    }
    logger.info(Debug.toString("numEnabledCandCells", numEnabledCandCells));
    Cell[][] enabledCellsForCand = enabledCandCells(enabledCandDepth, candidates);
    for (Cell[] cs : enabledCellsForCand) {
      if (cs.length != numEnabledCandCells) {
        throw new AssertionError();
      }
    }

    latencyMetric.tick("computeAllStates");
    Map<State, Integer> numSameStates =
        computeAllStates(numCandPattern, numEnabledCandCells, enabledCellsForCand,
                         numCellsInProblem, cellsInProblem);
    latencyMetric.tack("computeAllStates");
    states = numSameStates.keySet().toArray(new State[0]);
    for (int i = 0; i < states.length; i++) {
      states[i].myId = i;
    }
    logger.info(Debug.toString("num states", states.length));
    for (State s : states) {
      s.possiblePairs = new ArrayList<State>();
    }

    Set<Cell>[][] forbiddenMoves =
        new Set[numCandPattern][numCandPattern]; // cand[i] cannot be moved by these dirs not to overlap with cand[j].
    int numForbiddenMoves = 0;
    for (int i = 0; i < numCandPattern; i++) {
      for (int j = 0; j < numCandPattern; j++) {
        forbiddenMoves[i][j] = new HashSet<Cell>();
        for (Cell c : enabledCellsForCand[i]) {
          for (Cell d : enabledCellsForCand[j]) {
            Cell dir = d.sub(c);
            forbiddenMoves[i][j].add(dir);
          }
        }
        numForbiddenMoves += forbiddenMoves[i][j].size();
      }
    }
    logger.info(Debug.toString("numForbiddenMoves", numForbiddenMoves));
    latencyMetric.tick("updatePossibleStatePairs");
    updatePossibleStatePairs(candidates, states.length, forbiddenMoves);
    cleanUpStates();
    for (State s : states) {
      for (State t : states) if (s != t) {
        if ((s.mask | t.mask) == t.mask) {
          if (t.possiblePairs.containsAll(s.possiblePairs)) {
            if (!s.possiblePairs.containsAll(t.possiblePairs) || s.myId > t.myId) {
              s.hopeless = true;
              break;
            }
          }
        }
      }
    }
    cleanUpStates();
    computeMaskToState();
    int numPromisingStates = 0;
    int numEdges = 0;

    for (State s : states) {
      if (!s.hopeless) {
        numPromisingStates++;
        numEdges += s.possiblePairs.size();
      }
    }

    logger.info(Debug.toString("num promising states:", numPromisingStates));
    logger.info(Debug.toString("num edges in the state graph:", numEdges));

    latencyMetric.tack("updatePossibleStatePairs");
    numCandidates = Math.min(numCandidates, numCellsInProblem);

    latencyMetric.tick("solving");
    List<State> ans = dfs(new ArrayList<State>(), 0, 0);
    latencyMetric.tack("solving");

    // Create result from ans.
    if (monitor != null) {
      monitor.setValue(0);
    }

    if (ans == null) {
      return null;
    }

    int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
    int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
    for (State state : ans) {
      for (int i = 0; i < numEnabledCandCells; i++) {
        Cell cell = enabledCellsForCand[state.candId][i].add(state.candMoveVec);
        minX = Math.min(minX, cell.x);
        maxX = Math.max(maxX, cell.x);
        minY = Math.min(minY, cell.y);
        maxY = Math.max(maxY, cell.y);
      }
    }
    int[][] res = new int[maxX - minX + 1][maxY - minY + 1];
    for (int i = 0; i < ans.size(); i++) {
      State state = ans.get(i);
      for (int j = 0; j < numEnabledCandCells; j++) {
        Cell cell = enabledCellsForCand[state.candId][j].add(state.candMoveVec);
        assert res[cell.x - minX][cell.y - minY] == 0;
        res[cell.x - minX][cell.y - minY] = i + 1;
      }
    }

    for (int i = 0; i < numCellsInProblem; i++) {
      Cell cell = cellsInProblem[i];
      assert res[cell.x - minX][cell.y - minY] > 0;
      res[cell.x - minX][cell.y - minY] = -res[cell.x - minX][cell.y - minY];
    }
    return res;
  }

  private void cleanUpStates() {
    boolean changed = false;
    int sz = states.length;
    states = removeHopeless(states);
    if (sz != states.length) {
      changed = true;
    }
    for (State s : states) {
      sz = s.possiblePairs.size();
      s.possiblePairs = Arrays.asList(removeHopeless(s.possiblePairs.toArray(new State[0])));
      if (s.possiblePairs.size() != sz) {
        changed = true;
      }
    }
    if (changed) {
      if (markHopeless()) {
        cleanUpStates();
      }
    }
  }

  private boolean markHopeless() {
    boolean hasHopeless = false;
    for (State s : states) {
      long mask = s.mask;
      for (State t : s.possiblePairs) {
        mask |= t.mask;
      }
      if (mask != (1L << numCellsInProblem) - 1) {
        s.hopeless = true;
        hasHopeless = true;
      }
    }
    return hasHopeless;
  }

  private State[] removeHopeless(State[] states) {
    List<State> res = new ArrayList<State>();
    for (State s : states) {
      if (!s.hopeless) {
        res.add(s);
      }
    }
    return res.toArray(new State[res.size()]);
  }

  private void updatePossibleStatePairs(Poly[] candidates, int numStates,
                                        Set<Cell>[][] forbiddenMoves) {
    computeMaskToState();

    int numAllCombination = numStates * maskToState.size();
    for (int i = 0, progress = 0; i < numStates; i++) {
      long mask = states[i].mask;
      // TODO: for state S, if there is a cell in problem such that any state cannot cover the cell,
      // we can remove the S from consideration.
      for (Map.Entry<Long, List<State>> e : maskToState.entrySet()) {
        progress++;
        if (monitor != null) {
          monitor.setValue(10 + progress * 40 / numAllCombination);
        }

        if ((states[i].mask & e.getKey()) != 0) {
          continue;
        }

        for (State s : e.getValue()) {
          Cell d = states[i].candMoveVec.sub(s.candMoveVec);
          if (forbiddenMoves[states[i].candId][s.candId].contains(d)) {
            continue;
          }

          int
              h =
              Math.min(candidates[states[i].candId].getHeight(), candidates[s.candId].getHeight());
          int
              w =
              Math.min(candidates[states[i].candId].getWidth(), candidates[s.candId].getWidth());
          if (Math.abs(d.x) < h - enabledCandDepth && Math.abs(d.y) < w - enabledCandDepth) {
            continue;
          }
          mask |= e.getKey();
//          if (i < s.myId) {
          states[i].possiblePairs.add(s);
//          }
        }
      }
      if (mask != (1L << numCellsInProblem) - 1) {
        states[i].hopeless = true;
      }
    }
  }

  private void computeMaskToState() {
    maskToState = new HashMap<Long, List<State>>();
    for (State s : states) {
      if (!maskToState.containsKey(s.mask)) {
        maskToState.put(s.mask, new ArrayList<State>());
      }
      maskToState.get(s.mask).add(s);
    }
    Long[] tmpMasks = maskToState.keySet().toArray(new Long[0]);
    Arrays.sort(tmpMasks, new Comparator<Long>() {
      @Override
      public int compare(Long o1, Long o2) {
        int c1 = Long.bitCount(o1), c2 = Long.bitCount(o2);
        if (c1 != c2) return - (c1 - c2); // Larger bit count.
        return - Long.compare(o1, o2);// Higher set bit.
      }
    });
    masks = new long[tmpMasks.length];
    for (int i = 0; i < masks.length; i++) {
      masks[i] = tmpMasks[i];
    }
  }

  private Map<State, Integer> computeAllStates(int numCandPattern, int numEnabledCandCells,
                                               Cell[][] enabledCellsForCand, int numCellInProblem,
                                               Cell[] cellsInProblem) {
    Map<State, Integer> numSameStates = new HashMap<State, Integer>();
    // Number of ways to place a cell in cand over a cell in problem.
    int numAllWaysToPut = numCandPattern * numEnabledCandCells * numCellInProblem;
    // Iterate over all such patterns.
    for (int i = 0, progress = 0; i < numCandPattern; i++) {
      for (int j = 0; j < numEnabledCandCells; j++) {
        for (int k = 0; k < numCellInProblem; k++) {
          progress++;
          if (monitor != null) {
            monitor.setValue(progress * 10 / numAllWaysToPut);
          }
          // cellsInProblem[k] に,enabledCellsForCand[i][j]
          // を重ねた場合を考えている.
          // dは,cand を,どれだけ動かせば,problem に重なるかを表す.
          Cell candMoveVec = cellsInProblem[k].sub(enabledCellsForCand[i][j]);
          State state = new State(i, candMoveVec);
          for (int l = 0; l < numCellInProblem; l++) {
            Cell orig = cellsInProblem[l].sub(candMoveVec);
            if (Arrays.asList(enabledCellsForCand[i]).contains(orig)) {
              state.mask |= 1L << l;
            }
          }
          if (!numSameStates.containsKey(state)) {
            numSameStates.put(state, 1);
          } else {
            numSameStates.put(state, numSameStates.get(state) + 1);
          }
        }
      }
    }
    return numSameStates;
  }

  private Cell[][] enabledCandCells(int validCellDepth, Poly[] candidates) {
    int numCandPattern = candidates.length;
    Cell[][] enabledCells = new Cell[numCandPattern][];
    for (int i = 0; i < numCandPattern; i++) {
      List<Cell> cellsOnPeripheral = getCellsOnPeripheral(candidates[i], validCellDepth);
      enabledCells[i] = cellsOnPeripheral.toArray(new Cell[cellsOnPeripheral.size()]);
    }
    return enabledCells;
  }

  private List<Cell> getCellsOnPeripheral(Poly poly, int depth) {
    List<Cell> cells = new ArrayList<Cell>();
    for (int i = 0; i < poly.getHeight(); i++) {
      for (int j = 0; j < poly.getWidth(); j++) {
        if (isCellOnPeripheral(poly, i, j, depth)) {
          cells.add(new Cell(i, j));
        }
      }
    }
    return cells;
  }

  private boolean isCellOnPeripheral(Poly poly, int x, int y, int depth) {
    int h = poly.getHeight(), w = poly.getWidth();
    if (!poly.get(x, y)) {
      return false;
    }
    if (depth <= x && x < h - depth && depth <= y && y < w - depth) {
      return false;
    }
    return true;
  }

  List<State> dfs(List<State> stateStack, long mask, int currentNumCands) {
    if (mask == (1L << numCellsInProblem) - 1) {
      return stateStack;
    }
    if (currentNumCands >= numCandidates) {
      return null;
    }

    long rest = ((1L << numCellsInProblem) - 1) ^ mask;
    boolean doMonitor = currentNumCands == 0;
    int numAllStates = 0;
    for (List<State> ss : maskToState.values()) {
      numAllStates += ss.size();
    }
    int cnt = 0;
    for (long cur : masks) {
      List<State> nxts = maskToState.get(cur);
      if (Long.highestOneBit(rest) != Long.highestOneBit(cur)) {
        cnt += nxts.size();
        continue;
      }
      loop:
      for (State s : nxts) {
        if (doMonitor) {
          monitor.setValue(50 + 50 * cnt / numAllStates);
        }
        cnt++;
        if (s.hopeless) {
          throw new AssertionError();
        }
        for (State t : stateStack) {
          if (!t.possiblePairs.contains(s) && !s.possiblePairs.contains(t)) {
            continue loop;
          }
        }
        stateStack.add(s);
        List<State> res = dfs(stateStack, mask | cur, currentNumCands + 1);
        if (res != null) {
          return res;
        }
        stateStack.remove(stateStack.size() - 1);
      }
    }
    return null;
  }

  private class State {

    int myId;
    int candId;
    Cell candMoveVec;
    //        int num;
    List<State> possiblePairs;
    // bit mask represents the cells in problem this state covers.
    long mask;
    // true if this state will never be a part of any solution.
    boolean hopeless;

    public State(int candId, Cell candMoveVec) {
      super();
      this.candId = candId;
      this.candMoveVec = candMoveVec;
    }

    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + candMoveVec.x;
      result = prime * result + candMoveVec.y;
      result = prime * result + candId;
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
      State other = (State) obj;
      if (candMoveVec.x != other.candMoveVec.x) {
        return false;
      }
      if (candMoveVec.y != other.candMoveVec.y) {
        return false;
      }
      if (candId != other.candId) {
        return false;
      }
      return true;
    }

    public String toString() {
      return "State [candMoveVec=" + candMoveVec + ", possiblePairs.size()=" + possiblePairs.size()
             + ", candId=" + candId + ", mask=" + mask + "]";
    }
  }

  /**
   * ポリオミノが連結であるかどうかを返す.
   */
  public static boolean isConnected(Poly poly) {
    int h = poly.getHeight(), w = poly.getWidth();
    boolean[][] cell = new boolean[h + 2][w + 2];
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        cell[i + 1][j + 1] = poly.get(i, j);
      }
    }
    return connected(cell);
  }

  private static boolean connected(boolean[][] cell) {
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

  private static final int[] dx = {1, 0, -1, 0};
  private static final int[] dy = {0, 1, 0, -1};

  private static void dfs(boolean[][] cell, int x, int y) {
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

  /**
   * ポリオミノが穴開きでなければ,trueを返す.
   */
  public static boolean noHole(Poly poly) {
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
}
