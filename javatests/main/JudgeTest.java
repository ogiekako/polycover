package main;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import util.Debug;

public class JudgeTest {

  private boolean[][] s2b(String[] ss) {
    boolean[][] res = new boolean[ss.length][ss[0].length()];
    for (int i = 0; i < res.length; i++) {
      for (int j = 0; j < res[0].length; j++) {
        res[i][j] = ss[i].charAt(j) == '#';
      }
    }
    return res;
  }

  @Test
  public void testJudge1() throws NoCellException {
    boolean[][] ac = s2b(new String[]{
        ".#.#.",
        "##.##",
        "#...#",
        "#####"
    });
    Poly candidate = new Poly(ac);
    boolean[][] acd = s2b(new String[]{
        "###",
        "###",
        "###"
    });
    Poly problem = new Poly(acd);
    Covering res = Judge.newBuilder(problem, candidate).build().judge().covering;

    Assert.assertNull(res);
  }

  @Test
  public void testJudge2() throws NoCellException {
    boolean[][] ac = s2b(new String[]{
        ".#...",
        "##.##",
        "#...#",
        "#####"
    });
    Poly candidate = new Poly(ac);
    boolean[][] acd = s2b(new String[]{
        "###",
        "###",
        "###"
    });
    Poly problem = new Poly(acd);
    Covering res = Judge.newBuilder(problem, candidate).build().judge().covering;
    Assert.assertNotNull(res);
  }

  @Test
  public void testJudge2_depth1() throws NoCellException {
    boolean[][] ac = s2b(new String[]{
        ".....",
        "##.##",
        "#...#",
        "#####"
    });
    Poly candidate = new Poly(ac);
    boolean[][] acd = s2b(new String[]{
        "###",
        "###",
        "###"
    });
    Poly problem = new Poly(acd);
    Covering
        res =
        Judge.newBuilder(problem, candidate).setMaxNumCands(2).setEnabledCandDepth(1).build()
            .judge().covering;
    Assert.assertTrue(res == null);
  }

  @Test
  public void testJudge2_depth2() throws NoCellException {
    boolean[][] ac = s2b(new String[]{
        ".....",
        "##.##",
        "#...#",
        "#####"
    });
    Poly candidate = new Poly(ac);
    boolean[][] acd = s2b(new String[]{
        "###",
        "###",
        "###"
    });
    Poly problem = new Poly(acd);
    Covering
        res =
        Judge.newBuilder(problem, candidate).setMaxNumCands(2).setEnabledCandDepth(2).build()
            .judge().covering;
    Assert.assertNotNull(res);
  }

  @Test
  public void testJudge2_num1() throws NoCellException {
    boolean[][] ac = s2b(new String[]{
        ".....",
        "##.##",
        "#...#",
        "#####"
    });
    Poly candidate = new Poly(ac);
    boolean[][] acd = s2b(new String[]{
        "###",
        "###",
        "###"
    });
    Poly problem = new Poly(acd);
    Covering res = Judge.newBuilder(problem, candidate).setMaxNumCands(1).build().judge().covering;
    Assert.assertTrue(Debug.toString(res), res == null);
  }

  @Test
  public void testJudge_minMaxCands_no() throws NoCellException {
    boolean[][] ac = s2b(new String[]{
        "###",
        "#.#",
        "###"
    });
    Poly candidate = new Poly(ac);
    boolean[][] acd = s2b(new String[]{
        "###",
        "#.#",
        "###"
    });
    Poly problem = new Poly(acd);
    Covering
        res =
        Judge.newBuilder(problem, candidate).setMinNumCands(2).setMaxNumCands(3).build()
            .judge().covering;
    Assert.assertNull(Debug.toString(res), res);
  }

  @Test
  public void testJudge_minMaxCands_yes() throws NoCellException {
    boolean[][] ac = s2b(new String[]{
        "###",
        "#.#",
        "###"
    });
    Poly candidate = new Poly(ac);
    boolean[][] acd = s2b(new String[]{
        "###",
        "#.#",
        "###"
    });
    Poly problem = new Poly(acd);
    Covering
        res =
        Judge.newBuilder(problem, candidate).setMinNumCands(1).setMaxNumCands(3).build()
            .judge().covering;
    Assert.assertNotNull(res);
  }

  @Test
  public void testJudge_minMaxCands_yes2() throws NoCellException {
    boolean[][] ac = s2b(new String[]{
        "###",
        "#.#",
        "###"
    });
    Poly candidate = new Poly(ac);
    boolean[][] acd = s2b(new String[]{
        "###",
        "#.#",
        "###"
    });
    Poly problem = new Poly(acd);
    Covering res = Judge.newBuilder(problem, candidate).setMinNumCands(2).setMaxNumCands(4).build()
        .judge().covering;
    Assert.assertNotNull(res);
  }

  @Test
  public void testJudge3() throws NoCellException {
    boolean[][] ac = new boolean[][]{
        {true}
    };
    Poly candidate = new Poly(ac);
    boolean[][] acd = new boolean[][]{
        {true}
    };
    Poly problem = new Poly(acd);
    Covering res = Judge.newBuilder(problem, candidate).build().judge().covering;
    Assert.assertTrue(res != null);
  }

  @Test
  public void testJudge4() throws NoCellException {
    boolean[][] ac = new boolean[][]{
        {true}
    };
    Poly candidate = new Poly(ac);
    boolean[][] acd = new boolean[][]{
        {true, true}
    };
    Poly problem = new Poly(acd);
    Covering res = Judge.newBuilder(problem, candidate).build().judge().covering;
    Assert.assertTrue(res != null);
  }

  @Test
  public void testJudge5() throws NoCellException {
    boolean[][] ac = s2b(new String[]{
        "##",
        "##"
    });
    Poly candidate = new Poly(ac);
    boolean[][] acd = s2b(new String[]{
        "###",
        "###",
        "###"
    });
    Poly problem = new Poly(acd);
    Covering res = new Judge(problem, candidate).judge().covering;

    Assert.assertTrue(res != null);
  }

  @Test
  public void testNumSolutions() throws Exception {
    class TC {

      String prob;
      String cand;
      long wantNumSolutions;

      public TC(String prob, String cand, long wantNumSolutions) {
        this.cand = cand;
        this.prob = prob;
        this.wantNumSolutions = wantNumSolutions;
      }
    }

    List<TC> tcs = Arrays.asList(
        new TC("1 1\n#", "1 1\n#", 1),
        new TC("1 2\n##", "1 1\n#", 1),
        new TC("1 3\n###", "1 1\n#", 1),
        new TC("1 2\n##", "1 2\n##", 1 + 3 * 3),
        new TC("1 3\n###", "1 2\n##", 1 * 3 + 3 * 1 + 3 * 2 * 3)
    );
    for (TC tc : tcs) {
      Poly prob = Poly.load(new Scanner(tc.prob));
      Poly cand = Poly.load(new Scanner(tc.cand));
      Debug.debug("prob:", prob);
      Debug.debug("cand", cand);
      Judge.Result result = Judge.newBuilder(prob, cand)
          .setAlsoNumSolutions()
          .build().judge();
      Assert.assertEquals(tc.wantNumSolutions, result.numWayOfCovering);
    }
  }
}
