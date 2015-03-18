package cui;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import main.NoCellException;
import main.PolyArray;

public class Judger {

  /*
  Usage:
  java -cp bin cui.Main [--maxdepth=d] [--min_num_cand=o] [--max_num_cand=0] problemFile candFile
  --maxdepth=d は、最大深さを指定する。覆うのに使えるセルは、バウンディングボックスから深さ d までのセルであり、
  差し込める深さも d までとなる。
  --min_num_cand=o
  --max_num_cand=o はそれぞれ、覆うのに使用するポリオミノの最小、最大枚数をしていする。
   */
  public static void main(String[] args) throws FileNotFoundException, NoCellException {
    int p = 0;
    int maxDepth = Integer.MAX_VALUE / 2;
    int minNumCand = 1;
    int maxNumCand = Integer.MAX_VALUE;
    while (args[p].startsWith("--")) {
      String opt = args[p++];
      if (opt.startsWith("--maxdepth")) {
        maxDepth = Integer.valueOf(opt.replace("--maxdepth=", ""));
      }
      if (opt.startsWith("--min_num_cand")) {
        minNumCand = Integer.valueOf(opt.replace("--min_num_cand=", ""));
      }
      if (opt.startsWith("--max_num_cand")) {
        maxNumCand = Integer.valueOf(opt.replace("--max_num_cand=", ""));
      }
    }
    String problemFileName = args[p++];
    String candFileName = args[p++];
    Scanner problemIn = new Scanner(new File(problemFileName));
    Scanner candIn = new Scanner(new File(candFileName));
    PolyArray problem = PolyArray.load(problemIn);
    PolyArray cand = PolyArray.load(candIn);
    boolean ok =
        main.Judge.newBuilder(problem, cand)
            .setEnabledCandDepth(maxDepth)
            .setMinNumCands(minNumCand)
            .setMaxNumCands(maxNumCand).build().judge() == null;
    System.out.println(ok ? "OK" : "NG");
  }
}
