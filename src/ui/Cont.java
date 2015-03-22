package ui;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;

import javax.swing.*;

import ai.AI;
import ai.AIOption;
import ai.BestResultMonitor;
import ai.Result;
import main.Cell;
import main.Covering;
import main.Judge;
import main.NoCellException;
import main.Poly;
import main.PolyAnalyzer;
import main.ProgressMonitor;
import ui.view.View;

public class Cont implements AbstCont, ProgressMonitor {

  static Logger logger = Logger.getLogger(Cont.class.getName());

  int numCandHistory = 20;
  public Stack<Model> candHistory = new Stack<Model>();
  public final Model cand;
  public final Model problem;
  public CoveringModel wa;
  boolean rotSym = false;
  boolean revRotSym = false;
  boolean realTime = false;
  //// Judge ////
  public int maxNumCand = Integer.MAX_VALUE;
  public int minNumCand = 1;
  public int validCellDepth = Integer.MAX_VALUE;
  //// AI ////
  public AIOption aiOption = new AIOption();
  public long objective;
  SwingWorker<Integer, Integer> currentWorker = null;

  private boolean running;

  public Cont() {
    cand = new Model(null);
    problem = new Model(new Poly(new boolean[8][8]));
    wa = new CoveringModel();
  }

  public void setCand(Poly cand) {
    this.cand.setPoly(cand);
    updateView();
  }

  public void setProblem(Poly _covered) {
    this.problem.setPoly(_covered);
    updateView();
  }

  private final List<View> viewList = new ArrayList<View>();
  private final List<main.ProgressMonitor> monitorList = new ArrayList<main.ProgressMonitor>();

  @Override
  public void addView(View view) {
    viewList.add(view);
  }

  @Override
  public void updateView() {
    for (View view : viewList) {
      view.update();
    }
  }

  public void flip(Model model, int x, int y) {
    if (model == cand) {
      flipCover(x, y);
    } else if (model == problem) {
      model.flip(x, y);
    } else {
      assert false;
    }
    updateView();
  }

  private Cell rot90(Cell sz, Cell c) {
    return new Cell(c.y, sz.y - 1 - c.x);
  }

  private Cell rev(Cell sz, Cell c) {
    return new Cell(c.x, sz.y - 1 - c.y);
  }

  private void flipCover(int x, int y) {
    Cell sz = new Cell(cand.getHeight(), cand.getWidth());
    Cell c = new Cell(x, y);
    Set<Cell> visited = new HashSet<Cell>();
    if (rotSym) {
      assert cand.getHeight() == cand.getWidth();
      assert !revRotSym;
      for (int i = 0; i < 4; i++) {
        if (!visited.contains(c)) {
          cand.flip(c.x, c.y);
          visited.add(c);
        }
        c = rot90(sz, c);
      }
    } else if (revRotSym) {
      assert cand.getHeight() == cand.getWidth();
      assert !rotSym;
      for (int i = 0; i < 2; i++) {
        for (int j = 0; j < 4; j++) {
          if (!visited.contains(c)) {
            cand.flip(c.x, c.y);
            visited.add(c);
          }
          c = rot90(sz, c);
        }
        c = rev(sz, c);
      }
    } else {
      cand.flip(c.x, c.y);
    }
    updateView();

    if (realTime) {
      judge(null);
    }
  }

  public void save(File file) throws IOException {
    PrintWriter pw = new PrintWriter(file);
    pw.println("covered:");
    pw.println(problem.getHeight() + " " + problem.getWidth());
    for (int i = 0; i < problem.getHeight(); i++) {
      String s = "";
      for (int j = 0; j < problem.getWidth(); j++) {
        s += problem.get(i, j) ? "#" : ".";
      }
      pw.println(s);
    }

    pw.println("cover:");
    pw.println(cand.getHeight() + " " + cand.getWidth());
    for (int i = 0; i < cand.getHeight(); i++) {
      String s = "";
      for (int j = 0; j < cand.getWidth(); j++) {
        s += cand.get(i, j) ? "#" : ".";
      }
      pw.println(s);
    }
    pw.flush();
  }

  /**
   * load let the user to select the file to read, and install the cand and problem the file
   * represents to the model of cand and problem. File chooser will remember the last directory a
   * file is selected from.
   */
  public void load(File file) throws IOException {
    Scanner sc = new Scanner(file);
    ProblemAndCand problemAndCand = loadProblemAndCand(sc);

    if (problemAndCand.cand == null && problemAndCand.problem == null) {
      if (file.getPath().endsWith(".ans") || file.getPath().endsWith(".dup")) {
        try {
          problemAndCand.cand = Poly.load(new Scanner(file));
        } catch (Exception e) {
          String msg = "failed to read file as a candidate";
          logger.warning(msg + ":\n" + e.toString());

          throw new IOException(msg, e);
        }
      } else {
        try {
          problemAndCand.problem = Poly.load(new Scanner(file));
        } catch (Exception e) {
          String msg = "failed to read file as a problem";
          logger.warning(msg + ":\n" + e.toString());
          throw new IOException(msg, e);
        }
      }
    }
    if (problemAndCand.cand != null) {
      this.cand.setPoly(problemAndCand.cand);
    }
    if (problemAndCand.problem != null) {
      this.problem.setPoly(problemAndCand.problem);
    }
    updateView();
  }

  private ProblemAndCand loadProblemAndCand(Scanner sc) {
    ProblemAndCand problemAndCand = new ProblemAndCand();
    while (sc.hasNext()) {
      String command = sc.next();
      if (command.equals("covered:")) {
        problemAndCand.problem = Poly.load(sc);
      }
      if (command.equals("cover:")) {
        problemAndCand.cand = Poly.load(sc);
      }
    }
    return problemAndCand;
  }

  public void undo() {
    cand.undo();
    updateView();
  }

  class ProblemAndCand {

    Poly problem;
    Poly cand;
  }

  public void judge(final JFrame parent) {
    if (running) {
      return;
    }
    SwingWorker<Integer, Integer> sw = new SwingWorker<Integer, Integer>() {
      protected Integer doInBackground() throws Exception {
        running = true;
        judgeInBackground(parent);
        running = false;
        return 0;
      }
    };
    sw.execute();
  }

  private void judgeInBackground(JFrame parent) {
    Covering res;
    try {
      res = Judge.newBuilder(problem.getPoly(), cand.getPoly())
          .setMinNumCands(minNumCand)
          .setMaxNumCands(maxNumCand)
          .setEnabledCandDepth(validCellDepth)
          .setMonitor(this).build().judge().covering;
    } catch (NoCellException ex) {
      JOptionPane.showMessageDialog(parent, "board is empty.");
      return;
    }
    wa.setCovering(res);
    if (res == null) {
      PolyAnalyzer analyzer = PolyAnalyzer.of(cand.getPoly());
      if (!analyzer.isConnected()) {
        JOptionPane.showMessageDialog(parent, "OK, but not connected.");
      } else if (!analyzer.hasNoHole()) {
        JOptionPane.showMessageDialog(parent, "OK, but contains hole(s).");
      } else {
        JOptionPane.showMessageDialog(parent, "OK.");
      }
    } else {
      updateView();
    }
  }

  public void setRotSym(boolean b) {
    rotSym = b;
    updateView();
  }

  public void setRevRotSym(boolean b) {
    revRotSym = b;
    updateView();
  }

  public void expand() {
    int h = cand.getHeight(), w = cand.getWidth();
    boolean[][] nxt = new boolean[h + 2][w + 2];
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        nxt[i + 1][j + 1] = cand.get(i, j);
      }
    }
    cand.setPoly(new Poly(nxt));
    updateView();
  }

  public void setRealTimeMode(boolean b) {
    realTime = b;
    updateView();
  }

  public void setMinNumCand(int minNumCand) {
    this.minNumCand = minNumCand;
    updateView();
  }

  public void setMaxNumCand(int maxNumCand) {
    this.maxNumCand = maxNumCand;
    updateView();
  }

  public void setValidCellDepth(int i) {
    validCellDepth = i;
    updateView();
  }

  //// AI ////
  public void setAiOption(AIOption opt) {
    this.aiOption = opt;
  }

  AI ai;
  public static interface AICallback {
    void done(boolean aborted, Result best);
  }

  public void ai(final AICallback callback) {
    currentWorker = new SwingWorker<Integer, Integer>() {

      @Override
      protected Integer doInBackground() throws Exception {
        ai = AI.builder(problem.getPoly())
            .setOption(aiOption)
            .setMonitor(Cont.this)
            .addBestResultMonitor(
                new BestResultMonitor() {
                  @Override
                  public void update(Result result) {
                    Cont.this.setCand(result.convertedCand);
                  }
                }).build();
        Result best = ai.solve(cand.getPoly());
        cand.setPoly(best.convertedCand);
        objective = best.objective;
        updateView();
        callback.done(ai.abort, best);
        return 0;
      }
    };
    currentWorker.execute();
  }

  public void abortAI() {
    if (ai != null) {
      ai.abort();
    }
  }

  /**
   * monitorには,0~100のあいだの数値を与える.
   */
  public void addProgressMonitor(main.ProgressMonitor monitor) {
    monitorList.add(monitor);
  }

  private int prevProgress = 0;

  public void setValue(int n) {
    if (prevProgress == n) {
      return;
    }
    prevProgress = n;
    for (main.ProgressMonitor monitor : monitorList) {
      monitor.setValue(n);
    }
  }

  public boolean isRunning() {
    return running;
  }
}
