package ai;

public class AIOption {

  public int maxIter = 100;
  public boolean rotSym = true;
  public boolean revRotSym = false;
  public int queueSize = 5;
  public Evaluator objective = Evaluator.DepthIn2;
  public Validator validator = Validator.Connected;

  @Override
  public String toString() {
    return "Option{" +
           ", maxIter=" + maxIter +
           ", rotSym=" + rotSym +
           ", revRotSym=" + revRotSym +
           ", queueSize=" + queueSize +
           ", objective=" + objective.name() +
           '}';
  }
}
