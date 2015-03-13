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
    accum.put(name, accum.get(name) + System.currentTimeMillis() - start.get(name));
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
      long timeMs;

      @Override
      public int compareTo(E o) {
        return Long.compare(timeMs, o.timeMs);
      }
    }
    List<E> es = new ArrayList<E>();
    for (Map.Entry<String, Long> et : accum.entrySet()) {
      E e = new E();
      e.name = et.getKey();
      e.timeMs = et.getValue();
      es.add(e);
    }
    Collections.sort(es);
    for (E e : es) {
      b.append(String.format("%s: %s.\n", e.name, toStr(e.timeMs)));
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
