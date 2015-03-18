package ai;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Scanner;

import main.Judge;
import main.Poly;
import main.PolyArray;
import util.Debug;

public class AITest {

  @Test
  public void testSolve() throws Exception {
    Poly problem = PolyArray.load(new Scanner("3 3\n"
                                              + "###\n"
                                              + "###\n"
                                              + "###"));
    Poly seed = PolyArray.load(new Scanner("6 6\n"
                                           + "......\n"
                                           + "......\n"
                                           + "......\n"
                                           + "##.##.\n"
                                           + "#...#.\n"
                                           + "#####."));
    AI.Option opt = new AI.Option();
    opt.rotSym = false;
    opt.allowHole = false;
    AI.Result result = AI.builder(problem).setOption(opt).build().solve(seed);
    Assert.assertEquals(AI.INF, result.maxAllowableDepth);
    Debug.debug(result.convertedCand);
    Assert.assertTrue(Judge.newBuilder(problem, result.convertedCand)
                          .setMinNumCands(2)
                          .setMaxNumCands(2).build().judge() == null);
  }
}