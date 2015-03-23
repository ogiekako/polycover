package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

import ai.AI;
import ai.AIOption;
import ai.Evaluator;
import ai.Result;
import ai.Validator;
import main.Judge;
import main.NoCellException;
import main.Poly;
import main.ProgressMonitor;
import main.Stopwatch;

public class SearchAll {

  public static void main(String[] args) throws Exception {
    new SearchAll().run();
  }

  class E implements Comparable<E> {

    Poly prob;
    Poly cand;
    int depth;

    public E(Poly prob, Poly cand, int depth) {
      this.prob = prob;
      this.cand = cand;
      this.depth = depth;
    }

    @Override
    public int compareTo(E o) {
      // deeper is better.
      return -(depth - o.depth);
    }
  }

  private void run() throws FileNotFoundException, NoCellException {
    List<Poly> probs = FileUtil.allPolysUnder(new File("problem/7"), ".no");
    List<Poly> cands = FileUtil.allPolysUnder(new File("ans"), ".ans");
    List<E> res = new ArrayList<E>();
    ProgressMonitor monitor = new ProgressMonitor() {
      int prog = 0;

      @Override
      public void setValue(int n) {
        while (prog < n) {
          prog++;
          System.err.print(".");
        }
      }
    };
    int numProb = probs.size();
    int cnt = 0;
    Stopwatch latency = new Stopwatch();
    for (Poly prob : probs) {
      cnt++;
      monitor.setValue(cnt * 100 / numProb);
      for (Poly cand : cands) {
        Judge.Builder builder = Judge.newBuilder(prob, cand).setMinNumCands(2).setMaxNumCands(2).setLatencyMetric(latency);
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
        res.add(new E(prob, cand, d));
      }
    }
    System.err.println("latency: " + latency.summary());
    System.err.println("");
    Collections.sort(res);
    HashMap<String, Integer> seen = new HashMap<String, Integer>();
    HashSet<String> solved = new HashSet<String>();
    AI.logger.setLevel(Level.OFF);
    for (E e : res) {
      if (!seen.containsKey(e.prob.filePath())) {
        seen.put(e.prob.filePath(), 0);
      }
      if (seen.get(e.prob.filePath()) >= 5) {
        continue;
      }
      seen.put(e.prob.filePath(), seen.get(e.prob.filePath()) + 1);
      System.out.println(e.prob.filePath() + " " + e.cand.filePath() + " " + e.depth);

      if (!solved.contains(e.prob.filePath())) {
        System.err.println("trying AI");
        Poly prob = e.prob;
        Poly seed = e.cand;
        AIOption opt = new AIOption();
        opt.rotSym = true;
        opt.revRotSym = false;
        opt.queueSize = 20;
        opt.maxIter = 200;
        opt.objective = Evaluator.DepthAndNumSolutionsIn2;
        opt.validator = Validator.AllowHoleDisallowDiagonal;
        Result result = AI.builder(prob)
            .setOption(opt)
            .setLatencyMetric(latency)
            .build().solve(seed);
        File tmp;
        try {
          tmp = File.createTempFile("poly", "maybe.ans");
          FileUtil.savePoly(result.convertedCand, tmp);
        } catch (IOException e1) {
          System.err.println("Failed to create tmp file.");
          continue;
        }
        if (result.objective == Evaluator.INF) {
          System.out.println("Maybe solution:");
          System.out.println(prob.filePath() + " " + tmp.getPath());
          solved.add(prob.filePath());
        } else {
          System.err.println(prob.filePath() + " " + tmp.getPath() + " " + result.objective);
        }
        System.err.println("latency: " + latency.summary());
      }
    }
  }
}
