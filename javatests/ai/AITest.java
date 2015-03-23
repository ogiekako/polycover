package ai;


import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

  @Test
  public void testFindAnswer() throws Exception {
    class TestCase {

      String problemPath;
      String seedPath;

      public TestCase(String probPath, String seedPath) {
        this.problemPath = probPath;
        this.seedPath = seedPath;
      }
    }
    List<TestCase> tests = Arrays.asList(
        new TestCase("problem/8/1111000_0001111.yes", "ans/6/T.ans"),
        new TestCase("problem/8/111100_001111.yes", "ans/6/T.ans")
    );

    List<TestCase> badTest = new ArrayList<TestCase>();
    for (TestCase tc : tests) {
      Poly prob = Poly.load(new Scanner(new File(tc.problemPath)));
      Poly seed = Poly.load(new Scanner(new File(tc.seedPath)));

      Assert.assertNotNull(Judge.newBuilder(prob, seed.clone()).build().judge());

      AIOption opt = new AIOption();
      opt.validator = Validator.AllowHoleDisallowDiagonal;
      opt.objective = Evaluator.DepthAndNumSolutionsIn2;
      opt.maxIter = 100;
      Result result = AI.builder(prob).setOption(opt).build().solve(seed.clone());
      if (result.objective < Evaluator.INF) {
        System.err.println(tc.problemPath + " " + tc.seedPath + " " + result.objective);
        badTest.add(tc);
      }
    }
    Assert.assertEquals(0, badTest.size());
  }
}
