package ai;

import java.util.Random;

import main.Covering;
import main.Poly;

class State implements Comparable<State> {

  private static Random rnd = new Random(141281412L);
  Poly cand;
  // INF if infinity (cand can be a solution).
  long objective;
  Covering covering;
  int hash;
  long id;

  public State(Poly cand, long objective, Covering covering) {
    this.cand = cand;
    this.objective = objective;
    this.covering = covering;
    this.id = rnd.nextLong();
  }

  @Override
  public int hashCode() {
    if (hash != 0) {
      return hash;
    }
    return hash = cand.hashCode();
  }

  @Override
  public int compareTo(State o) {
    if (objective != o.objective) {
      // larger objective function is better.
      return -Long.compare(objective, o.objective);
    } else {
      // tie is broken randomly
      return Long.compare(id, o.id);
    }
  }

  @Override
  public String toString() {
    return "State{" +
           "cand=" + cand +
           ", objective=" + objective +
           ", covering=" + covering +
           ", hash=" + hash +
           ", id=" + id +
           '}';
  }
}
