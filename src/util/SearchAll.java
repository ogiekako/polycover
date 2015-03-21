package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import main.Judge;
import main.NoCellException;
import main.Poly;
import main.ProgressMonitor;

public class SearchAll {

  public static void main(String[] args) throws Exception {
    new SearchAll().run();
  }

  class E implements Comparable<E> {

    String probPath;
    String candPath;
    int depth;

    public E(String probPath, String candPath, int depth) {
      this.probPath = probPath;
      this.candPath = candPath;
      this.depth = depth;
    }

    @Override
    public int compareTo(E o) {
      // deeper is better.
      return -(depth - o.depth);
    }
  }

  private void run() throws FileNotFoundException, NoCellException {
    List<Poly> probs = FileUtil.allPolysUnder(new File("problem"), ".no");
    List<Poly> cands = FileUtil.allPolysUnder(new File("ans"), ".ans");
    List<E> res = new ArrayList<E>();
    ProgressMonitor monitor = new ProgressMonitor() {
      int prog=0;
      @Override
      public void setValue(int n) {
        while(prog < n) {
          prog++;
          System.err.print(".");
        }
      }
    };
    int numProb = probs.size();
    int cnt = 0;
    for (Poly prob : probs) {
      cnt++;
      monitor.setValue(cnt * 100 / numProb);
      for (Poly cand : cands) {
        Judge.Builder builder = Judge.newBuilder(prob, cand).setMinNumCands(2).setMaxNumCands(2);
        int d = 0;
        boolean ok = true;
        for (int n = 32; n > 0; n /= 2) {
          builder.setEnabledCandDepth(d + n);
          Judge.Result result = builder.build().judge();
          if (result.covering == null) {
            d += n;
          } else {
            ok = false;
          }
        }
        if (ok) {
          d = 1000;
        }
        res.add(new E(prob.filePath(), cand.filePath(), d));
      }
    }
    Collections.sort(res);
    HashMap<String, Integer> seen = new HashMap<String, Integer>();
    for (E e : res) {
      if (!seen.containsKey(e.probPath)) {
        seen.put(e.probPath, 0);
      }
      if (seen.get(e.probPath) >= 2) {
        continue;
      }
      seen.put(e.probPath, seen.get(e.probPath) + 1);
      System.out.println(e.probPath + " " + e.candPath + " " + e.depth);
    }
  }
}
