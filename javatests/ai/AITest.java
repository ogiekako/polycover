package ai;


import org.junit.Assert;
import org.junit.Test;

import java.util.Scanner;

import main.Judge;
import main.Poly;
import util.Debug;

public class AITest {

  @Test
  public void testSolve() throws Exception {
    Poly problem = Poly.load(new Scanner("3 3\n"
                                         + "###\n"
                                         + "###\n"
                                         + "###"));
    Poly seed = Poly.load(new Scanner("6 6\n"
                                      + "......\n"
                                      + "......\n"
                                      + "......\n"
                                      + "##.##.\n"
                                      + "#...#.\n"
                                      + "#####."));
    AIOption opt = new AIOption();
    opt.rotSym = false;
    opt.revRotSym = false;
    opt.objective = Evaluator.DepthIn2;
    Result result = AI.builder(problem).setOption(opt).build().solve(seed.clone());
    Assert.assertEquals(Evaluator.INF, result.objective);
    Debug.debug(result.convertedCand);
    Assert.assertTrue(Judge.newBuilder(problem, result.convertedCand)
                          .setMinNumCands(2)
                          .setMaxNumCands(2).build().judge().covering == null);

    opt.objective = Evaluator.DepthAndNumSolutionsIn2;
    result = AI.builder(problem).setOption(opt).build().solve(seed.clone());
    Assert.assertEquals(Evaluator.INF, result.objective);
    Debug.debug(result.convertedCand);
    Assert.assertTrue(Judge.newBuilder(problem, result.convertedCand)
                          .setMinNumCands(2)
                          .setMaxNumCands(2).build().judge().covering == null);

    opt.objective = Evaluator.DepthAndNumSolutionsIn23;
    opt.maxIter = 200;
    opt.validator = Validator.NoSeparate;
    result = AI.builder(problem).setOption(opt).build().solve(seed.clone());
    Assert.assertEquals(Evaluator.INF, result.objective);
    Debug.debug(result.convertedCand);
    Assert.assertTrue(Judge.newBuilder(problem, result.convertedCand)
                          .setMinNumCands(2)
                          .setMaxNumCands(3).build().judge().covering == null);
  }
}
