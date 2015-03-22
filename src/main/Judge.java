package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import util.Debug;

public class Judge {

  public static Logger logger = Logger.getLogger(Judge.class.getName());

  static {
    logger.setLevel(Level.OFF);
  }

  // the problem, or the polyomino to be covered.
  Poly problem;
  // the candidate, or the polyomino whose multiple copies are used to cover the target.
  Poly candidate;
  // min number of candidates can be used. In other words, at least this number of candidates
  // should be used to cover the problem.
  Option opt = new Option();
  private Cell[][] enabledCellsForCand;
  private Cell[] cellsInProblem;

  private static class Option {

    static int INF = (int) 1e9;

    int minNumCands = 1;
    // max number of candidates can be used.
    int maxNumCands = INF;
    int allowedCandDepth = INF;
    ProgressMonitor monitor = ProgressMonitor.DO_NOTHING;
    Stopwatch latencyMetric = Stopwatch.DO_NOTHING;
    boolean alsoNumSolutions = false;

    @Override
    public String toString() {
      return "Option{" +
             "minNumCands=" + minNumCands +
             ", maxNumCands=" + maxNumCands +
             ", allowedCandDepth=" + allowedCandDepth +
             ", alsoNumSolutions=" + alsoNumSolutions +
             '}';
    }
  }

  int numCellsInProblem;
  ArrayList<Candidate> candidates;
  List<Node> nodes;
  long[] masks;
  Map<Long, List<Node>> maskToState;
  int offset;
  long numSolutions;
  private Poly boundingPoly;
  private Poly innerPoly;

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

    public Builder setMonitor(ProgressMonitor monitor) {
      judge.opt.monitor = monitor;
      return this;
    }

    public Builder setMinNumCands(int minNumCands) {
      judge.opt.minNumCands = minNumCands;
      return this;
    }

    public Builder setMaxNumCands(int maxNumCands) {
      judge.opt.maxNumCands = maxNumCands;
      return this;
    }

    public Builder setEnabledCandDepth(int enabledCandDepth) {
      judge.opt.allowedCandDepth = enabledCandDepth;
      return this;
    }

    public Builder setLatencyMetric(Stopwatch latencyMetric) {
      judge.opt.latencyMetric = latencyMetric;
      return this;
    }

    public Builder setAlsoNumSolutions() {
      judge.opt.alsoNumSolutions = true;
      return this;
    }

    public Judge build() {
      return judge;
    }
  }

  public class Result {

    // covering == null if there is no solution.
    public final Covering covering;
    public final long numWayOfCovering;

    public Result(Covering covering, long numWayOfCovering) {
      this.covering = covering;
      this.numWayOfCovering = numWayOfCovering;
    }
  }

  /**
   * problem が candidate で,重ならずに覆えるかどうかを判定する. 覆えない場合は,null を返す. 覆える場合は,覆った状態を表すint[][] を返す. 0は空白,1~
   * が candidate のある位置を表し, -1 ~ が, problem と candidate が重なっていることを表す. maxNumCands は,使用する candidate
   * の最大枚数を示す. allowedCandDepth,有効なセルのバウンディングボックスからの距離を示す.例えば,1なら,周辺のセルしかみない.
   * また,その内側にはブロックがおいてあるかのように,入れない.
   */
  public Result judge() throws NoCellException {
    candidate = candidate.clone();
    logger.info(Debug.toString("Judge start with opt: ", opt));

    opt.latencyMetric.tick("initParams");
    int numEnabledCandCells = initParams();
    opt.latencyMetric.tack("initParams");

    List<Node> ans = solve(numEnabledCandCells);
    logger.info("ans: " + ans);

    // Create result from ans.
    if (opt.monitor != null) {
      opt.monitor.setValue(0);
    }

    if (ans == null) {
      return new Result(null, 0);
    }
    int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
    int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
    for (Node node : ans) {
      for (int i = 0; i < numEnabledCandCells; i++) {
        Cell cell = enabledCellsForCand[node.candId][i].add(node.candMoveVec);
        minX = Math.min(minX, cell.x);
        maxX = Math.max(maxX, cell.x);
        minY = Math.min(minY, cell.y);
        maxY = Math.max(maxY, cell.y);
      }
    }
    int[][] res = new int[maxX - minX + 1][maxY - minY + 1];
    for (int i = 0; i < ans.size(); i++) {
      Node node = ans.get(i);
      for (int j = 0; j < numEnabledCandCells; j++) {
        Cell cell = enabledCellsForCand[node.candId][j].add(node.candMoveVec);
        assert res[cell.x - minX][cell.y - minY] == 0;
        res[cell.x - minX][cell.y - minY] = i + 1;
      }
    }

    for (int i = 0; i < numCellsInProblem; i++) {
      Cell cell = cellsInProblem[i];
      assert res[cell.x - minX][cell.y - minY] > 0;
      res[cell.x - minX][cell.y - minY] = -res[cell.x - minX][cell.y - minY];
    }
    loop:
    for (int i = 0; i < 2; i++) {
      res = flip(res);
      for (int j = 0; j < 4; j++) {
        res = rot(res);
        if (hasCandAsIs(res)) {
          break loop;
        }
      }
      if (i == 1) {
        Debug.debug("res:");
        for (int[] a : res) {
          Debug.debug(a);
        }
        Debug.debug("cand:\n", candidate);
        throw new AssertionError();
      }
    }
    return new Result(new Covering().setArray(res), numSolutions);
  }

  // (i,j) -> (j,w-1-i)
  private int[][] rot(int[][] res) {
    int h = res.length, w = res[0].length;
    int[][] a = new int[w][h];
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        a[j][h - 1 - i] = res[i][j];
      }
    }
    return a;
  }

  // reverse each row.
  private int[][] flip(int[][] res) {
    int[][] a = new int[res.length][res[0].length];
    for (int i = 0; i < a.length; i++) {
      for (int j = 0; j < a[0].length; j++) {
        a[i][j] = res[i][a[0].length - 1 - j];
      }
    }
    return a;
  }

  private boolean hasCandAsIs(int[][] res) {
    boolean[][] bs = new boolean[res.length][res[0].length];
    for (int i = 0; i < res.length; i++) {
      for (int j = 0; j < res[0].length; j++) {
        bs[i][j] = Math.abs(res[i][j]) == 1;
      }
    }
    Poly p = new Poly(bs);
    return p.trim().equals(candidate.trim());
  }

  private List<Node> solve(int numEnabledCandCells) {
    logger.info("solve");
    opt.latencyMetric.tick("computeAllStates");
    nodes = computeAllStates(numEnabledCandCells, enabledCellsForCand,
                             numCellsInProblem, cellsInProblem);
    opt.latencyMetric.tack("computeAllStates");
    for (int i = 0; i < nodes.size(); i++) {
      nodes.get(i).myId = i;
    }
    logger.info(Debug.toString("num states", nodes.size()));

    opt.latencyMetric.tick("computeForbiddenMovesPrep");
    boolean[][][][] forbiddenMoves =
        new boolean[candidates.size()][candidates.size()][offset * 2][offset * 2];

    Cell[][] innerPlusCands = new Cell[candidates.size()][];
    for (int i = 0; i < candidates.size(); i++) {
      List<Cell> innerPlusCand = new ArrayList<Cell>();
      innerPlusCand.addAll(Arrays.asList(toCells(candidates.get(i).inner)));
      innerPlusCand.addAll(Arrays.asList(toCells(candidates.get(i).cand)));
      innerPlusCands[i] = innerPlusCand.toArray(new Cell[innerPlusCand.size()]);
    }
    opt.latencyMetric.tack("computeForbiddenMovesPrep");
    opt.latencyMetric.tick("computeForbiddenMoves");
    for (int i = 0; i < candidates.size(); i++) {
      for (int j = 0; j < innerPlusCands[i].length; j++) {
        Cell c = innerPlusCands[i][j];
        for (int k = 0; k < j; k++) {
          Cell d = innerPlusCands[i][k];
          int dx = d.x - c.x;
          int dy = d.y - c.y;
          forbiddenMoves[i][i][dx + offset][dy + offset] = true;
          forbiddenMoves[i][i][-dx + offset][-dy + offset] = true;
        }
      }
      for (int j = 0; j < i; j++) {
        for (Cell c : innerPlusCands[i]) {
          boolean[][] forbid = forbiddenMoves[i][j];
          for (Cell d : innerPlusCands[j]) {
            int dx = d.x - c.x;
            int dy = d.y - c.y;
            if (!forbid[dx + offset][dy + offset]) {
              forbid[dx + offset][dy + offset] = true;
              forbiddenMoves[j][i][-dx + offset][-dy + offset] = true;
            }
          }
        }
      }
    }

    opt.latencyMetric.tack("computeForbiddenMoves");

    computeMaskToState();

    long all = (1L << numCellsInProblem) - 1;
    List<Node> res = null;
    // with 1 cand
    int num1 = 0;
    if (opt.minNumCands <= 1 && 1 <= opt.maxNumCands) {
      if (maskToState.containsKey(all)) {
        num1++;
        res = Collections.singletonList(maskToState.get(all).get(0));
        if (!opt.alsoNumSolutions) {
          return res;
        }
      }
    }
    logger.info("#ways with 1 = " + num1);
    numSolutions += num1;

    // With 2 cands
    opt.latencyMetric.tick("with2cands");
    List<Node> resIn2 = with2cands(forbiddenMoves);
    opt.latencyMetric.tack("with2cands");
    if (!opt.alsoNumSolutions && resIn2 != null) {
      return resIn2;
    }
    if (res == null) {
      res = resIn2;
    }
    if (opt.maxNumCands <= 2) {
      return res;
    }
    opt.latencyMetric.tick("generateGraph");
    generateGraph(forbiddenMoves);
    opt.latencyMetric.tack("generateGraph");
    opt.latencyMetric.tick("cleanUpStates1");
    cleanUpStates();
    opt.latencyMetric.tack("cleanUpStates1");
    opt.latencyMetric.tick("computeUseless");
    if (!opt.alsoNumSolutions) {
      for (Node s : nodes) {
        for (Node t : nodes) {
          if (s != t) {
            if ((s.mask | t.mask) == t.mask) {
              if (t.possiblePairs.containsAll(s.possiblePairs)) {
                if (t.possiblePairs.size() > s.possiblePairs.size() || s.myId > t.myId) {
                  s.hopeless = true;
                  break;
                }
              }
            }
          }
        }
      }
    }
    opt.latencyMetric.tack("computeUseless");
    opt.latencyMetric.tick("cleanUpStates2");
    cleanUpStates();
    computeMaskToState();
    opt.latencyMetric.tack("cleanUpStates2");
    int numPromisingStates = 0;
    int numEdges = 0;

    for (Node s : nodes) {
      if (!s.hopeless) {
        numPromisingStates++;
        numEdges += s.possiblePairs.size();
      }
    }

    logger.info(Debug.toString("num promising states:", numPromisingStates));
    logger.info(Debug.toString("num edges in the state graph:", numEdges));

    opt.maxNumCands = Math.min(opt.maxNumCands, numCellsInProblem);

    opt.latencyMetric.tick("solving");
    List<Node> ans = dfs(new ArrayList<Node>(), 0, 0);
    opt.latencyMetric.tack("solving");
    if (res == null) {
      res = ans;
    }
    return res;
  }

  private Bits[] shift(Bits[] board, int k) {
    Bits[] res = new Bits[board.length];
    for (int i = 0; i < res.length; i++) {
      res[i] = board[i].shift(k);
    }
    return res;
  }

  //  private List<Node> with2cands(Bits[][][] forbiddenMoves) {
  private List<Node> with2cands(boolean[][][][] forbiddenMoves) {
    long all = (1L << numCellsInProblem) - 1;
    List<Node> res = null;
    int num2 = 0;
    if (opt.minNumCands <= 2 && 2 <= opt.maxNumCands) {
      for (Node v : nodes) {
        if ((v.mask & 1) != 1) {
          continue;
        }
        long rest = all ^ v.mask;
        if (!maskToState.containsKey(rest)) {
          continue;
        }
        for (Node u : maskToState.get(rest)) {
          if (canPutTogether(forbiddenMoves, u, v)) {
            num2++;
            if (res == null) {
              res = new ArrayList<Node>();
              res.add(v);
              res.add(u);
            }
            if (!opt.alsoNumSolutions) {
              return res;
            }
          }
        }
      }
    }
    numSolutions += num2;
    logger.info("#ways with 2 = " + num2);
    return res;
  }

  private int initParams() throws NoCellException {
    offset = Math.max(candidate.getHeight(), candidate.getWidth()) +
             Math.max(problem.getHeight(), problem.getWidth()) + 10;
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
    cellsInProblem = new Cell[numCellsInProblem];
    for (int i = 0, k = 0; i < problem.getHeight(); i++) {
      for (int j = 0; j < problem.getWidth(); j++) {
        if (problem.get(i, j)) {
          cellsInProblem[k++] = new Cell(i, j);
        }
      }
    }
    opt.monitor.setValue(0);

    makeBoundingPoly(candidate);
    makeInnerPoly(boundingPoly, opt.allowedCandDepth);
    for (int i = 0; i < candidate.getHeight(); i++) {
      for (int j = 0; j < candidate.getWidth(); j++) {
        if (candidate.get(i, j) && innerPoly.get(i, j)) {
          candidate.flip(i, j);
        }
      }
    }
//    candidate = new Poly(cellsOnPeripheral);
    LinkedHashSet<Candidate> candSet = new LinkedHashSet<Candidate>();
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 4; j++) {
        candSet.add(new Candidate(candidate, boundingPoly, innerPoly));
        candidate = candidate.rot90();
        boundingPoly = boundingPoly.rot90();
        innerPoly = innerPoly.rot90();
      }
      candidate = candidate.flip();
      boundingPoly = boundingPoly.flip();
      innerPoly = innerPoly.flip();
    }
    candidates = new ArrayList<Candidate>(candSet);
    enabledCellsForCand = enabledCandCells();
    return enabledCellsForCand[0].length;
  }

  private static class Candidate {

    Poly cand;
    Poly bound;
    Poly inner;

    public Candidate(Poly cand, Poly bound, Poly inner) {
      this.bound = bound;
      this.cand = cand;
      this.inner = inner;
    }

    @Override
    public boolean equals(Object o) {
      Candidate candidate = (Candidate) o;

      if (!cand.equals(candidate.cand)) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      return cand.hashCode();
    }
  }

  private void makeInnerPoly(Poly boundingPoly, int depth) {
    if (depth >= Option.INF) {
      innerPoly = new Poly(boundingPoly.getHeight(), boundingPoly.getWidth());
      return;
    }
    int m = depth == 0 ? 1 : depth * 4;
    int[] dx = new int[m], dy = new int[m];
    m = 0;
    for (int x = -depth; x <= depth; x++) {
      for (int y = -depth; y <= depth; y++) {
        if (Math.abs(x) + Math.abs(y) == depth) {
          dx[m] = x;
          dy[m] = y;
          m++;
        }
      }
    }
    int h = boundingPoly.getHeight(), w = boundingPoly.getWidth();
    innerPoly = boundingPoly.clone();
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        if (!innerPoly.get(i, j)) {
          continue;
        }
        for (int d = 0; d < m; d++) {
          int nx = i + dx[d], ny = j + dy[d];
          if (!get(boundingPoly, nx, ny)) {
            innerPoly.flip(i, j);
            break;
          }
        }
      }
    }
  }

  private boolean get(Poly poly, int x, int y) {
    int h = poly.getHeight(), w = poly.getWidth();
    return 0 <= x && x < h && 0 <= y && y < w && poly.get(x, y);
  }

  private void cleanUpStates() {
    boolean changed = false;
    int sz = nodes.size();
    nodes = removeHopeless(nodes);
    if (sz != nodes.size()) {
      changed = true;
    }
    for (Node s : nodes) {
      sz = s.possiblePairs.size();
      s.possiblePairs = new HashSet<Node>(removeHopeless(s.possiblePairs));
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
    for (Node s : nodes) {
      long mask = s.mask;
      for (Node t : s.possiblePairs) {
        mask |= t.mask;
      }
      if (mask != (1L << numCellsInProblem) - 1) {
        s.hopeless = true;
        hasHopeless = true;
      }
    }
    return hasHopeless;
  }

  private List<Node> removeHopeless(Collection<Node> nodes) {
    List<Node> res = new ArrayList<Node>();
    for (Node s : nodes) {
      if (!s.hopeless) {
        res.add(s);
      }
    }
    return res;
  }

  //  private void generateGraph(Bits[][][] forbiddenMoves) {
  private void generateGraph(boolean[][][][] forbiddenMoves) {
    int numAllCombination = nodes.size() * maskToState.size();
    int progress = 0;
    long[] masks = new long[nodes.size()];
    for (Node v : nodes) {
      masks[v.myId] |= v.mask;
      for (Map.Entry<Long, List<Node>> e : maskToState.entrySet()) {
        progress++;
        opt.monitor.setValue(10 + progress * 40 / numAllCombination);

        if ((v.mask & e.getKey()) != 0) {
          continue;
        }

        opt.latencyMetric.tick("genGraphInner");

        for (Node u : e.getValue()) {
          if (u.hopeless) {
            continue;
          }
          if (u.myId < v.myId) {
            continue;
          }
          boolean canPutTogether = canPutTogether(forbiddenMoves, v, u);
          if (!canPutTogether) {
            continue;
          }
          masks[v.myId] |= e.getKey();
          masks[u.myId] |= v.mask;
          v.possiblePairs.add(u);
          u.possiblePairs.add(v);
        }
        opt.latencyMetric.tack("genGraphInner");
      }
      if (masks[v.myId] != (1L << numCellsInProblem) - 1) {
        v.hopeless = true;
      }
    }
  }

  private boolean canPutTogether(boolean[][][][] forbiddenMoves, Node v, Node u) {
    int dx = v.candMoveVec.x - u.candMoveVec.x;
    int dy = v.candMoveVec.y - u.candMoveVec.y;
    if (forbiddenMoves[v.candId][u.candId][dx + offset][dy + offset]) {
      return false;
    }
    return true;
  }

  private void computeMaskToState() {
    maskToState = new HashMap<Long, List<Node>>();
    for (Node s : nodes) {
      if (!maskToState.containsKey(s.mask)) {
        maskToState.put(s.mask, new ArrayList<Node>());
      }
      maskToState.get(s.mask).add(s);
    }
    Long[] tmpMasks = maskToState.keySet().toArray(new Long[0]);
    Arrays.sort(tmpMasks, new Comparator<Long>() {
      @Override
      public int compare(Long o1, Long o2) {
        int c1 = Long.bitCount(o1), c2 = Long.bitCount(o2);
        if (c1 != c2) {
          return -(c1 - c2); // Larger bit count.
        }
        return -Long.compare(o1, o2);// Higher set bit.
      }
    });
    masks = new long[tmpMasks.length];
    for (int i = 0; i < masks.length; i++) {
      masks[i] = tmpMasks[i];
    }
  }

  private List<Node> computeAllStates(int numEnabledCandCells,
                                      Cell[][] enabledCellsForCand,
                                      int numCellInProblem,
                                      Cell[] cellsInProblem) {
    int numCandPattern = candidates.size();
    Set<Node> states = new HashSet<Node>();
    // Number of ways to place a cell in cand over a cell in problem.
    int numAllWaysToPut = numCandPattern * numEnabledCandCells * numCellInProblem;
    // Iterate over all such patterns.
    for (int i = 0, progress = 0; i < numCandPattern; i++) {
      Set<Cell> cells = new HashSet<Cell>(Arrays.asList(enabledCellsForCand[i]));
      for (int j = 0; j < numEnabledCandCells; j++) {
        for (int k = 0; k < numCellInProblem; k++) {
          progress++;
          if (opt.monitor != null) {
            opt.monitor.setValue(progress * 10 / numAllWaysToPut);
          }
          // cellsInProblem[k] に,enabledCellsForCand[i][j]
          // を重ねた場合を考えている.
          // dは,cand を,どれだけ動かせば,problem に重なるかを表す.
          Cell candMoveVec = cellsInProblem[k].sub(enabledCellsForCand[i][j]);
          Node node = new Node(i, candMoveVec);
          for (int l = 0; l < numCellInProblem; l++) {
            Cell orig = cellsInProblem[l].sub(candMoveVec);
            if (cells.contains(orig)) {
              node.mask |= 1L << l;
            }
          }
          states.add(node);
        }
      }
    }
    return new ArrayList<Node>(states);
  }

  private Cell[][] enabledCandCells() {
    int numCandPattern = candidates.size();
    Cell[][] enabledCells = new Cell[numCandPattern][];
    for (int i = 0; i < candidates.size(); i++) {
      enabledCells[i] = toCells(candidates.get(i).cand);
    }
    return enabledCells;
  }

  private Cell[] toCells(Poly poly) {
    List<Cell> cs = new ArrayList<Cell>();
    for (int i = 0; i < poly.getHeight(); i++) {
      for (int j = 0; j < poly.getWidth(); j++) {
        if (poly.get(i, j)) {
          cs.add(new Cell(i, j));
        }
      }
    }
    return cs.toArray(new Cell[cs.size()]);
  }

  /*
  bounding poly: a cell A is in the bounding poly if A is marked or there are two cells such that
  A is between the cells.
  A cell is in inner poly with depth D if all the cells within manhattan distance D from the cell
  are bounding poly. Other cells are said on peripheral.
   */
  private void makeBoundingPoly(Poly poly) {
    int h = poly.getHeight(), w = poly.getWidth();
    int[][] cnt = new int[h][w];
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        if (poly.get(i, j)) {
          break;
        }
        cnt[i][j]++;
      }
      for (int j = w - 1; j >= 0; j--) {
        if (poly.get(i, j)) {
          break;
        }
        cnt[i][j]++;
      }
    }
    for (int j = 0; j < w; j++) {
      for (int i = 0; i < h; i++) {
        if (poly.get(i, j)) {
          break;
        }
        cnt[i][j]++;
      }
      for (int i = h - 1; i >= 0; i--) {
        if (poly.get(i, j)) {
          break;
        }
        cnt[i][j]++;
      }
    }
    boundingPoly = new Poly(h, w);
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        if (cnt[i][j] < 2) {
          boundingPoly.flip(i, j);
        }
      }
    }
  }

  List<Node> dfs(List<Node> nodeStack, long mask, int currentNumCands) {
    if (mask == (1L << numCellsInProblem) - 1) {
      boolean isSol = currentNumCands >= opt.minNumCands;
      if (isSol) {
        if (currentNumCands > 2) {
          numSolutions++;
        }
        return nodeStack;
      }
      return null;
    }
    if (currentNumCands >= opt.maxNumCands) {
      return null;
    }

    long rest = ((1L << numCellsInProblem) - 1) ^ mask;
    boolean doMonitor = currentNumCands == 0;
    int numAllStates = 0;
    for (List<Node> ss : maskToState.values()) {
      numAllStates += ss.size();
    }
    int cnt = 0;
    List<Node> res = null;
    for (long cur : masks) {
      List<Node> nxts = maskToState.get(cur);
      if (Long.highestOneBit(rest) != Long.highestOneBit(cur)) {
        cnt += nxts.size();
        continue;
      }
      loop:
      for (Node s : nxts) {
        if (doMonitor) {
          opt.monitor.setValue(50 + 50 * cnt / numAllStates);
        }
        cnt++;
        if (s.hopeless) {
          throw new AssertionError();
        }
        for (Node t : nodeStack) {
          if (!t.possiblePairs.contains(s) && !s.possiblePairs.contains(t)) {
            continue loop;
          }
        }
        nodeStack.add(s);
        List<Node> curRes = dfs(nodeStack, mask | cur, currentNumCands + 1);
        if (curRes != null) {
          if (!opt.alsoNumSolutions) {
            return curRes;
          }
          if (res == null) {
            res = new ArrayList<Node>(curRes);
          }
        }
        nodeStack.remove(nodeStack.size() - 1);
      }
    }
    return res;
  }

}
