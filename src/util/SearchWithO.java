package util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import ai.AI;
import ai.AIOption;
import ai.BestResultMonitor;
import ai.Evaluator;
import ai.Result;
import ai.Validator;
import main.Poly;

public class SearchWithO {

  public static void main(String[] args) {
    new SearchWithO().run();
  }

  class E implements Comparable<E> {

    String prob;
    String cand;
    long obj;

    public E(long obj, String prob, String cand) {
      this.obj = obj;
      this.prob = prob;
      this.cand = cand;
    }

    @Override
    public int compareTo(E o) {
      return -Long.compare(obj, o.obj);
    }
  }

  private void run() {
    AI.logger.setLevel(Level.OFF);
    List<Poly> probs = FileUtil.allPolysUnder(new File("problem/7"), ".no");
    List<Poly> seeds = new ArrayList<Poly>();
    seeds.addAll(FileUtil.allPolysUnder(new File("ans/6/O.ans"), ""));
    List<E> cands = new ArrayList<E>();
    for (Poly prob : probs) {
      AIOption opt = new AIOption();
      opt.queueSize = 50;
      opt.rotSym = true;
      opt.revRotSym = true;
      opt.maxIter = 1000;
      opt.objective = Evaluator.DepthAndNumSolutionsIn23;
      opt.validator = Validator.AllowHoleDisallowDiagonal;
      Result result = AI.builder(prob).setOption(opt)
          .addBestResultMonitor(new BestResultMonitor() {
            @Override
            public void update(Result result) {
              System.err.println("obj: " + result.objective);
            }
          })
          .build().solve(seeds);

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
      } else {
        System.err.println(prob.filePath() + " " + tmp.getPath() + " " + result.objective);
        cands.add(new E(result.objective, prob.filePath(), tmp.getPath()));
      }
    }
    Collections.sort(cands);
    for (E e : cands) {
      System.out.println(e.prob + " " + e.cand + " " + e.obj);
    }
  }
}
