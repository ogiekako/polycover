package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Not thread safe.
public class Stopwatch {

  Map<String, Long> accum = new HashMap<String, Long>();
  Map<String, Long> start = new HashMap<String, Long>();
  Map<String, Long> latestLap = new HashMap<String, Long>();
  String previousName = null;

  public void tick(String name) {
    if (start.containsKey(name)) {
      throw new IllegalArgumentException("Already ticked.");
    }
    previousName = name;
    start.put(name, System.currentTimeMillis());
  }

  public void tack(String name) {
    if (!start.containsKey(name)) {
      throw new IllegalArgumentException("Already tacked.");
    }
    if (!accum.containsKey(name)) {
      accum.put(name, 0L);
    }
    long lap = System.currentTimeMillis() - start.get(name);
    latestLap.put(name, lap);
    accum.put(name, accum.get(name) + lap);
    start.remove(name);
  }

  public long get(String name) {
    return accum.containsKey(name) ? 0 : accum.get(name);
  }

  public static Stopwatch DO_NOTHING = new Stopwatch() {
    @Override
    public void tick(String name) {
      // Do nothing
    }

    @Override
    public void tack(String name) {
      // Do nothing
    }

    @Override
    public long get(String name) {
      return 0;
    }
  };

  public String summary() {
    StringBuilder b = new StringBuilder();
    class E implements Comparable<E> {

      String name;
      long totalMs;
      long lapMs;

      @Override
      public int compareTo(E o) {
        return Long.compare(totalMs, o.totalMs);
      }
    }
    List<E> es = new ArrayList<E>();
    for (String name : accum.keySet()) {
      E e = new E();
      e.name = name;
      e.totalMs = accum.get(name);
      e.lapMs = latestLap.get(name);
      es.add(e);
    }
    Collections.sort(es);
    for (E e : es) {
      b.append(String.format("%s: lap %s, total %s.\n", e.name, toStr(e.lapMs), toStr(e.totalMs)));
    }
    return b.toString();
  }

  private String toStr(long ms) {
    double min = 60 * 1000;
    if (ms < min) {
      return String.format("%fs", ms / 1e3);
    }
    double hour = min * 60;
    if (ms < hour) {
      return String.format("%fm", ms / min);
    }
    return String.format("%fh", ms / hour);
  }
}
