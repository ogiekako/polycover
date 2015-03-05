package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ui.AbstProgressMonitor;

public class Judge {
    /**
     * problem が candidate で,重ならずに覆えるかどうかを判定する.
     * 覆えない場合は,null を返す.
     * 覆える場合は,覆った状態を表すint[][] を返す.
     * 0は空白,1~ が candidate のある位置を表し, -1 ~ が, problem と candidate が重なっていることを表す.
     * numCandidates は,使用する candidate の最大枚数を示す.
     * enabledCandDepth,有効なセルのバウンディングボックスからの距離を示す.例えば,1なら,周辺のセルしかみない.
     * また,その内側にはブロックがおいてあるかのように,入れない.
     *
     * @param problem        - the problem, or the polyomino to be covered.
     * @param candidate      - the candidate, or the polyomino whose multiple copies are used to cover the target.
     * @param numCandidates  - number of candidates that can be used.
     * @param enabledCandDepth
     * @return
     * @throws NoCellException
     */
    public static int[][] judge(AbstPoly problem, AbstPoly candidate, int numCandidates, int enabledCandDepth, AbstProgressMonitor monitor) throws NoCellException {
        if (candidate == null) throw new NoCellException();

        if (monitor != null) {
            monitor.setValue(0);
        }

        candidate = candidate.trim();
        HashSet<AbstPoly> candSet = new HashSet<AbstPoly>();
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 4; j++) {
                candSet.add(candidate);
                candidate = candidate.rot90();
            }
            candidate = candidate.flip();
        }
        AbstPoly[] candidates = candSet.toArray(new AbstPoly[candSet.size()]);

        int numCandPattern = candidates.length;

        int numEnabledCandCells = getCellsOnPeripheral(candidate, enabledCandDepth).size();
        if (numEnabledCandCells == 0) throw new NoCellException();
        Cell[][] enabledCellsForCand = enabledCandCells(enabledCandDepth, candidates);
        for (Cell[] cs : enabledCellsForCand) if (cs.length != numEnabledCandCells) throw new AssertionError();

        // TODO: extract method.
        int numCellInProblem = 0;
        for (int i = 0; i < problem.getHeight(); i++)
            for (int j = 0; j < problem.getWidth(); j++)
                if (problem.get(i, j)) numCellInProblem++;
        Cell[] cellsInProblem = new Cell[numCellInProblem];
        for (int i = 0, k = 0; i < problem.getHeight(); i++)
            for (int j = 0; j < problem.getWidth(); j++)
                if (problem.get(i, j)) cellsInProblem[k++] = new Cell(i, j);

        Map<State, Integer> numSameStates = new HashMap<State, Integer>();
        // Number of ways to place a cell in cand over a cell in problem.
        int numAllWaysToPut = numCandPattern * numEnabledCandCells * numCellInProblem;
        // Iterate over all such patterns.
        for (int i = 0, progress = 0; i < numCandPattern; i++)
            for (int j = 0; j < numEnabledCandCells; j++)
                for (int k = 0; k < numCellInProblem; k++) {
                    progress++;
                    if (monitor != null) monitor.setValue(progress * 10 / numAllWaysToPut);
                    // cellsInProblem[k] に,enabledCellsForCand[i][j]
                    // を重ねた場合を考えている.
                    // dは,cand を,どれだけ動かせば,problem に重なるかを表す.
                    Cell candMoveVec = cellsInProblem[k].sub(enabledCellsForCand[i][j]);
                    State state = new State(i, candMoveVec);
                    if (!numSameStates.containsKey(state)) numSameStates.put(state, 1);
                    else numSameStates.put(state, numSameStates.get(state) + 1);
                }

        State[] states = numSameStates.keySet().toArray(new State[0]);
        for (State s : states)
            s.num = numSameStates.get(s);
        int numStates = states.length;
        for (State s : states) s.possiblePairs = new ArrayList<State>();

        int numAllCombination = numStates * (numStates - 1) / 2;
        for (int i = 0, progress = 0; i < numStates; i++) {
            // TODO: for state S, if there is a cell in problem such that any state cannot be used for covering,
            // we can remove the S from consideration.

            // Cand moved according to states[i].
            Set<Cell> movedCand = new HashSet<Cell>();
            for (int j = 0; j < numEnabledCandCells; j++) {
                movedCand.add(enabledCellsForCand[states[i].candId][j].add(states[i].candMoveVec));
            }
            for (int j = i + 1; j < numStates; j++) {
                progress++;
                if (monitor != null) monitor.setValue(10 + progress * 40 / numAllCombination);

                Cell d = states[i].candMoveVec.sub(states[j].candMoveVec);

                int h = Math.min(candidates[states[i].candId].getHeight(), candidates[states[j].candId].getHeight());
                int w = Math.min(candidates[states[i].candId].getWidth(), candidates[states[j].candId].getWidth());
                if (Math.abs(d.x) < h - enabledCandDepth && Math.abs(d.y) < w - enabledCandDepth)
                    continue;

                boolean overlapping = false;
                for (int k = 0; k < numEnabledCandCells; k++) {
                    if (movedCand.contains(enabledCellsForCand[states[j].candId][k].add(states[j].candMoveVec))) {
                        overlapping = true;
                        break;
                    }
                }
                if (!overlapping) {
                    states[i].possiblePairs.add(states[j]);
                }
            }
        }

        List<State> ans = null;
        numCandidates = Math.min(numCandidates, numCellInProblem);

        int all3 = numCandidates * numStates;

        loop:
        for (int i = 0, progress = 0; i < numCandidates; i++) {
            Set<State> visited = new HashSet<State>();
            for (State state : states) {

                progress++;
                // [50, 100]
                if (monitor != null) monitor.setValue(50 + progress * 50 / all3);

                List<State> init = new ArrayList<State>();
                init.add(state);
                visited.add(state);
                ans = dfs(init, 0, state.num, numCellInProblem, i, 0);
                if (ans != null) {
                    break loop;
                }
            }
        }

        if (monitor != null) monitor.setValue(0);

        if (ans == null) return null;

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

        for (int i = 0; i < numCellInProblem; i++) {
            Cell cell = cellsInProblem[i];
            assert res[cell.x - minX][cell.y - minY] > 0;
            res[cell.x - minX][cell.y - minY] = -res[cell.x - minX][cell.y - minY];
        }
        return res;
    }

    private static Cell[][] enabledCandCells(int validCellDepth, AbstPoly[] candidates) {
        int numCandPattern = candidates.length;
        Cell[][] enabledCells = new Cell[numCandPattern][];
        for (int i = 0; i < numCandPattern; i++) {
            List<Cell> cellsOnPeripheral = getCellsOnPeripheral(candidates[i], validCellDepth);
            enabledCells[i] = cellsOnPeripheral.toArray(new Cell[cellsOnPeripheral.size()]);
        }
        return enabledCells;
    }

    private static List<Cell> getCellsOnPeripheral(AbstPoly poly, int depth) {
        List<Cell> cells = new ArrayList<Cell>();
        for (int i = 0; i < poly.getHeight(); i++)
            for (int j = 0; j < poly.getWidth(); j++)
                if (isCellOnPeripheral(poly, i, j, depth)) {
                    cells.add(new Cell(i,j));
                }
        return cells;
    }

    private static boolean isCellOnPeripheral(AbstPoly poly, int x, int y, int depth) {
        int h = poly.getHeight(), w = poly.getWidth();
        if (!poly.get(x, y)) return false;
        if (depth <= x && x < h - depth && depth <= y && y < w - depth)
            return false;
        return true;
    }

    static List<State> dfs(List<State> befStates, int befId, int sum, int obj, int maxDepth, int depth) {
        if (sum == obj) return befStates;
        if (depth >= maxDepth) return null;
        assert sum < obj;
        for (int i = befId; i < befStates.get(0).possiblePairs.size(); i++) {
            State nxtState = befStates.get(0).possiblePairs.get(i);
            boolean ok = true;
            for (int j = 1; j < befStates.size(); j++) {
                if (!befStates.get(j).possiblePairs.contains(nxtState)) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                befStates.add(nxtState);
                List<State> tmp = dfs(befStates, i, sum + nxtState.num, obj,
                        maxDepth, depth + 1);
                if (tmp != null) return tmp;
                befStates.remove(befStates.size() - 1);
            }
        }
        return null;
    }

    private static class State {
        int candId;
        Cell candMoveVec;
        int num;
        List<State> possiblePairs;

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
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            State other = (State) obj;
            if (candMoveVec.x != other.candMoveVec.x) return false;
            if (candMoveVec.y != other.candMoveVec.y) return false;
            if (candId != other.candId) return false;
            return true;
        }

        public String toString() {
            return "State [candMoveVec=" + candMoveVec + ", possiblePairs.size()=" + possiblePairs.size() + ", candId=" + candId + ", num=" + num + "]";
        }
    }

    /**
     * ポリオミノが連結であるかどうかを返す.
     *
     * @param poly
     * @return
     */
    public static boolean isConnected(AbstPoly poly) {
        int h = poly.getHeight(), w = poly.getWidth();
        boolean[][] cell = new boolean[h + 2][w + 2];
        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++)
                cell[i + 1][j + 1] = poly.get(i, j);
        return connected(cell);
    }

    private static boolean connected(boolean[][] cell) {
        int h = cell.length, w = cell[0].length;
        loop:
        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++)
                if (cell[i][j]) {
                    dfs(cell, i, j);
                    break loop;
                }
        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++)
                if (cell[i][j]) return false;
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
     *
     * @param poly
     * @return
     */
    public static boolean noHole(AbstPoly poly) {
        int h = poly.getHeight(), w = poly.getWidth();
        boolean[][] cell = new boolean[h + 2][w + 2];
        for (int i = 0; i < h + 2; i++)
            for (int j = 0; j < w + 2; j++)
                cell[i][j] = true;
        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++)
                cell[i + 1][j + 1] = !poly.get(i, j);
        return connected(cell);
    }

    public static int[][] judge(PolyArray problem, PolyArray candidate) throws NoCellException {
        return judge(problem, candidate, Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2);
    }

    public static int[][] judge(PolyArray problem, PolyArray candidate, int numCandidate, int validCellDepth) throws NoCellException {
        return judge(problem, candidate, numCandidate, validCellDepth, null);
    }
}
