package longtest;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import main.Covering;
import main.Judge;
import main.PolyArray;
import main.Stopwatch;
import util.Debug;
import util.FileUtil;

public class JudgeTestLong {

  @Test
  public void testT() throws Exception {
    Stopwatch latencyMetric = new Stopwatch();
    String probPath = "problem/6/T.yes";
    String ansPath = "ans/6/T.ans";
    Scanner probIn = new Scanner(new File(probPath));
    Scanner ansIn = new Scanner(new File(ansPath));
    PolyArray prob = PolyArray.load(probIn);
    PolyArray ans = PolyArray.load(ansIn);
    Covering result =
        Judge.newBuilder(prob, ans).setLatencyMetric(latencyMetric).build().judge().covering;
    Assert.assertNull(Debug.toString(result), result);

    System.err.println(latencyMetric.summary());
  }

  @Test
  public void testYes() throws Exception {
    Stopwatch latencyMetric = new Stopwatch();
    List<String> problems = FileUtil.allFilesUnder(new File("problem"));
    for (String probPath : problems) {
      if (probPath.endsWith(".yes")) {
        String ansPath = probPath.replaceAll("^problem/", "ans/").replaceAll("\\.yes$", ".ans");
        if (!new File(ansPath).exists()) {
          ansPath = probPath.replaceAll("^problem/", "ans/").replaceAll("\\.yes$", ".dup");
        }
        System.err.printf("%s %s\n", probPath, ansPath);
        Scanner probIn = new Scanner(new File(probPath));
        Scanner ansIn = new Scanner(new File(ansPath));
        PolyArray prob = PolyArray.load(probIn);
        PolyArray ans = PolyArray.load(ansIn);
        Covering result =
            Judge.newBuilder(prob, ans).setLatencyMetric(latencyMetric).build().judge().covering;
        Assert.assertNull(Debug.toString(result), result);
        System.err.println(latencyMetric.summary());
      }
    }
  }

  @Test
  public void testNo() throws Exception {
    List<String> testAnsPaths = FileUtil.allFilesUnder(new File("ans"));

    Stopwatch latencyMetric = new Stopwatch();
    List<String> problems = FileUtil.allFilesUnder(new File("problem"));
    List<String> answers = new ArrayList<String>();
    for (String probPath : problems) {
      if (probPath.endsWith(".no")) {
        for (String ansPath : testAnsPaths) {
          if (!ansPath.endsWith(".ans")) {
            continue;
          }
          String comb = String.format("%s %s", probPath, ansPath);
          Scanner probIn = new Scanner(new File(probPath));
          Scanner ansIn = new Scanner(new File(ansPath));
          PolyArray prob = PolyArray.load(probIn);
          PolyArray ans = PolyArray.load(ansIn);
          Covering result =
              Judge.newBuilder(prob, ans).setLatencyMetric(latencyMetric).build().judge().covering;
          if (result == null) {
            answers.add(comb);
          }
        }
      }
    }
    System.err.println(latencyMetric.summary());
    for (String s : answers) {
      System.err.println(s);
    }
    Assert.assertTrue(answers.isEmpty());
  }

}
