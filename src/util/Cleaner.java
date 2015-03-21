package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Scanner;

import main.Judge;
import main.NoCellException;
import main.Poly;
import main.PolyAnalyzer;
import main.PolyArray;

/**
 * Cleaner clean up files under ans/ and problem/.
 * Add ans/path/to/name.ans and run
 * java Cleaner
 * and files under ans/ problem/ are renamed as appropriately.
 */
public class Cleaner {

  public static void main(String[] args) throws Exception {
    int p = 0;
    boolean dryrun = false;
    while (p < args.length && args[p].startsWith("-")) {
      String s = args[p++];
      if (s.startsWith("-dryrun")) {
        dryrun = true;
      }
    }
    new Cleaner().run(dryrun);
  }

  boolean dryrun;

  private void run(boolean dryryn) throws Exception {
    this.dryrun = dryryn;
    List<Poly> probs = FileUtil.allPolysUnder(new File("problem"), "");
    List<Poly> cands = FileUtil.allPolysUnder(new File("ans"), "");
    // yes are valid
    checkYesIsValid(probs);

    // yes or no to meh
    moveToMeh(probs);

    // check cands are solutions and move .no to .yes
    for (Poly cand : cands) {
      Poly prob = getCorrespondingProblem(probs, cand);
      Judge.Result res =
          Judge.newBuilder(prob, cand).build().judge();
      if (res.covering != null) {
        throw new AssertionError(String.format("%s is not a solution for %s.",
                                               cand.filePath(), prob.filePath()));
      }
      if (prob.filePath().endsWith(".no")) {
        move(prob, prob.filePath().replace(".no", ".yes"));
      }
    }

    // move to meh again
    moveToMeh(probs);

    // remove solutions for .meh
    for (Poly cand : cands) {
      Poly prob = getCorrespondingProblem(probs, cand);
      if (prob.filePath().endsWith(".meh")) {
        rm(cand);
      }
    }

    // rename ans <-> dup
    for (Poly me : cands) {
      if (me.filePath() == null) {
        continue;
      }
      boolean hasDup = false;
      for (Poly you : cands) {
        if (you.filePath() == null) {
          continue;
        }
        if (you.filePath().compareTo(me.filePath()) >= 0) {
          continue;
        }
        // dup < ans
        if (you.equals(me)) {
          hasDup = true;
        }
      }
      String ext = hasDup ? ".dup" : ".ans";
      String nPath = me.filePath().replace(".dup", ext).replace(".ans", ext);
      if (!me.filePath().equals(nPath)) {
        move(me, nPath);
      }
    }
  }

  private void moveToMeh(List<Poly> probs) throws IOException {
    System.err.println("moveToMeh");
    for (Poly prob : probs) {
      for (Poly yes : probs) {
        if (prob == yes || !yes.filePath().endsWith(".yes")) {
          continue;
        }
        boolean meh = PolyAnalyzer.of(prob).contains(yes);
        if (meh) {
          if (!prob.filePath().endsWith(".meh")) {
            move(prob, prob.filePath().replaceAll("\\..*$", ".meh"));
          }
        }
      }
    }
  }

  private Poly getCorrespondingProblem(List<Poly> probs, Poly cand) {
    Poly prob = null;
    loop:
    for (Poly nProb : probs) {
      for (String ext : new String[]{".yes", ".no", ".meh"}) {
        String tmp =
            cand.filePath().replace(".dup", ext).replace(".ans", ext).replace("ans", "problem");
        if (tmp.equals(nProb.filePath())) {
          prob = nProb;
          break loop;
        }
      }
    }
    if (prob == null) {
      throw new AssertionError("problem for " + cand.filePath() + " was not found.");
    }
    return prob;
  }

  private void rm(Poly poly) {
    String orig = poly.filePath();
    poly.setFilePath(null);
    File file = new File(orig);
    System.err.printf("rm %s\n", orig);
    if (!dryrun) {
      boolean deleted = file.delete();
      if (!deleted) {
        System.err.printf("file %s was not deleted.\n", orig);
      }
    }
  }

  private void move(Poly poly, String path) throws IOException {
    String orig = poly.filePath();
    poly.setFilePath(path);
    String msg = String.format("mv %s %s", orig, path);
    System.err.println(msg);
    if (!dryrun) {
      Files.move(new File(orig).toPath(), new File(path).toPath());
    }
  }

  private void checkYesIsValid(List<Poly> probs) throws NoCellException, FileNotFoundException {
    System.err.println("checking yes are valid.");
    for (Poly prob : probs) {
      if (!prob.filePath().endsWith(".yes")) {
        continue;
      }
      String ansPath = prob.filePath().replace(".yes", ".ans").replace("problem", "ans");
      if (!new File(ansPath).exists()) {
        ansPath = ansPath.replace(".ans", ".dup");
        if (!new File(ansPath).exists()) {
          throw new AssertionError("No ans file for " + prob.filePath());
        }
      }
      Judge.Result result =
          Judge.newBuilder(prob, PolyArray.load(new Scanner(new File(ansPath)))).build().judge();
      if (result.covering != null) {
        throw new AssertionError(String.format("%s is not a solution for %s.",
                                               ansPath, prob.filePath()));
      }
    }
  }
}
