package ui;

import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.*;

import main.Cell;
import main.Judge;
import main.NoCellException;
import main.PolyAnalyzer;
import main.PolyArray;

public class Cont implements AbstCont, ProgressMonitor {

  static Logger logger = Logger.getLogger(Cont.class.getName());

  final Model cand;
  final Model problem;
  final WAModel wa;
  boolean rotSym = false;
  boolean revRotSym = false;
  boolean realTime = false;
  int maxNumCand = Integer.MAX_VALUE;
  int minNumCand = 1;
  int validCellDepth = Integer.MAX_VALUE;

  private boolean running;

  Cont() {
    cand = new Model(null);
    problem = new Model(new PolyArray(new boolean[20][20]));
    wa = new WAModel();
  }

  void setCand(PolyArray _cover) {
    this.cand.setPoly(_cover);
    updateView();
  }

  void setProblem(PolyArray _covered) {
    this.problem.setPoly(_covered);
    updateView();
  }

  private final List<AbstView> viewList = new ArrayList<AbstView>();
  private final List<ProgressMonitor> monitorList = new ArrayList<ProgressMonitor>();

  @Override
  public void addView(AbstView view) {
    viewList.add(view);
  }

  @Override
  public void updateView() {
    for (AbstView view : viewList) {
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
      run(null);
    }
  }

  public void save(Component parent) {
    JFileChooser chooser = new JFileChooser();
    chooser.showSaveDialog(parent);
    File file = chooser.getSelectedFile();

    PrintWriter pw;
    try {
      pw = new PrintWriter(file);
    } catch (Exception e) {
      error(parent, "cannot open the file");
      return;
    }
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

  private void error(Component parent, String msg) {
    if (msg == null) {
      msg = "something is wrong.";
    }
    JOptionPane.showMessageDialog(parent, msg);
  }

  /**
   * load let the user to select the file to read, and install the cand and problem the file
   * represents to the model of cand and problem. File chooser will remember the last directory a
   * file is selected from.
   *
   * @param parent the component that triggered this operation. This is used to determine the
   *               position where the file or error dialog is displayed.
   */
  public void load(Component parent, File file) {
    Scanner sc;
    try {
      sc = new Scanner(file);
    } catch (Exception e) {
      error(parent, "cannot open the file");
      return;
    }
    ProblemAndCand problemAndCand = loadProblemAndCand(sc);

    if (problemAndCand.cand == null && problemAndCand.problem == null) {
      if (file.getPath().endsWith(".ans")) {
        try {
          problemAndCand.cand = PolyArray.load(new Scanner(file));
        } catch (Exception e) {
          String msg = "failed to read file as a candidate";
          logger.warning(msg + ":\n" + e.toString());
          error(parent, msg);
        }
      } else {
        try {
          problemAndCand.problem = PolyArray.load(new Scanner(file));
        } catch (Exception e) {
          String msg = "failed to read file as a problem";
          logger.warning(msg + ":\n" + e.toString());
          error(parent, msg);
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
        problemAndCand.problem = PolyArray.load(sc);
      }
      if (command.equals("cover:")) {
        problemAndCand.cand = PolyArray.load(sc);
      }
    }
    return problemAndCand;
  }

  class ProblemAndCand {

    PolyArray problem;
    PolyArray cand;
  }

  private void runInBackGround(JFrame parent) {
    int[][] res;
    try {
      res = Judge.newBuilder(problem.poly, cand.poly)
          .setMinNumCands(minNumCand)
          .setMaxNumCands(maxNumCand)
          .setEnabledCandDepth(validCellDepth)
          .setMonitor(this).build().judge();
    } catch (NoCellException ex) {
      JOptionPane.showMessageDialog(parent, "board is empty.");
      return;
    }
    if (res == null) {
      PolyAnalyzer analyzer = PolyAnalyzer.of(cand.poly);
      if (!analyzer.isConnected()) {
        JOptionPane.showMessageDialog(parent, "OK. but not connected.");
      } else if (!analyzer.hasNoHole()) {
        JOptionPane.showMessageDialog(parent, "OK. but contains hole(s).");
      } else {
        JOptionPane.showMessageDialog(parent, "OK.");
      }
    } else {
      wa.setArray(res);
      updateView();
    }
  }

  public void run(final JFrame parent) {
    if (running) {
      return;
    }
    SwingWorker<Integer, Integer> sw = new SwingWorker<Integer, Integer>() {
      protected Integer doInBackground() throws Exception {
        running = true;
        runInBackGround(parent);
        running = false;
        return 0;
      }
    };
    sw.execute();
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
    cand.setPoly(new PolyArray(nxt));
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

  /**
   * monitorには,0~100のあいだの数値を与える.
   */
  public void addProgressMonitor(ProgressMonitor monitor) {
    monitorList.add(monitor);
  }

  private int prevProgress = 0;

  public void setValue(int n) {
    if (prevProgress == n) {
      return;
    }
    prevProgress = n;
    for (ProgressMonitor monitor : monitorList) {
      monitor.setValue(n);
    }
  }

  public boolean isRunning() {
    return running;
  }
}
