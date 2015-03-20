package ai;

public class AIOption {

  public int maxIter = 100;
  public boolean rotSym = true;
  public boolean revRotSym = false;
  public boolean allowHole = true;
  public boolean allowUnconnected = false;
  public int queueSize = 5;
  public Evaluator objective = Evaluator.DepthIn2;

  @Override
  public String toString() {
    return "Option{" +
           "allowHole=" + allowHole +
           ", maxIter=" + maxIter +
           ", rotSym=" + rotSym +
           ", revRotSym=" + revRotSym +
           ", allowUnconnected=" + allowUnconnected +
           ", queueSize=" + queueSize +
           ", objective=" + objective.name() +
           '}';
  }
}
