package ai;

import main.Poly;

public class Result {

  public final Poly convertedCand;
  public final long objective;

  public Result(Poly convertedCand, long objective) {
    this.convertedCand = convertedCand;
    this.objective = objective;
  }
}
