package ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class AppFrame extends JFrame {

  private final JFrame myself;
  private final Cont cont;

  private final PolyPanel polyPanel;
  private final MyMenuBar menuBar;
  private final MyProgressBar runProgressBar;

  public AppFrame(Cont cont) {
    myself = this;
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    this.cont = cont;

    polyPanel = new PolyPanel(cont.cand, cont);
    menuBar = new MyMenuBar();
    runProgressBar = new MyProgressBar();
    add(polyPanel);
    setJMenuBar(menuBar);
    add(runProgressBar, BorderLayout.SOUTH);
    cont.addProgressMonitor(runProgressBar);

    setVisible(true);
    setSize(600, 400);
  }

  class MyProgressBar extends JProgressBar implements ProgressMonitor {

    public void setValue(int n) {
      super.setValue(n);
    }
  }

  class MyMenuBar extends JMenuBar {

    private final JMenu fileMenu;
    private final JMenu editMenu;
    private final JMenu optionMenu;
    private final JMenu runMenu;

    public MyMenuBar() {
      fileMenu = new FileMenu(AppFrame.this, cont);
      this.add(fileMenu);

      editMenu = new JMenu("edit");
      this.add(editMenu);
      final JMenuItem setCoveredPoly = new JMenuItem("set problem poly");
      final JCheckBoxMenuItem rotSym = new JCheckBoxMenuItem("rot sym");
      final JCheckBoxMenuItem revRotSym = new JCheckBoxMenuItem("rev rot sym");
      final JMenuItem expand = new JMenuItem("expand");
      editMenu.add(setCoveredPoly);
      editMenu.add(rotSym);
      editMenu.add(revRotSym);
      editMenu.add(expand);
      setCoveredPoly.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          JDialog frame = new JDialog(myself);

          frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
          frame.setVisible(true);
          frame.setSize(420, 280);
          PolyPanel panel = new PolyPanel(cont.problem, cont);
          frame.add(panel);
        }
      });
      rotSym.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (rotSym.isSelected()) {
            if (revRotSym.isSelected()) {
              revRotSym.doClick();
            }
            cont.setRotSym(true);
          } else {
            cont.setRotSym(false);
          }
        }
      });
      revRotSym.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (revRotSym.isSelected()) {
            if (rotSym.isSelected()) {
              rotSym.doClick();
            }
            cont.setRevRotSym(true);
          } else {
            cont.setRevRotSym(false);
          }
        }
      });
      expand.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          cont.expand();
        }
      });

      optionMenu = new JMenu("option");
      this.add(optionMenu);
      final JCheckBoxMenuItem realTime = new JCheckBoxMenuItem("real time mode");
      final JMenuItem numCover = new JMenuItem("num cand");
      final JMenuItem validCellDepth = new JMenuItem("insert depth");
      optionMenu.add(realTime);
      optionMenu.add(numCover);
      optionMenu.add(validCellDepth);
      realTime.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (realTime.isSelected()) {
            cont.setRealTimeMode(true);
          } else {
            cont.setRealTimeMode(false);
          }
        }
      });
      numCover.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String s = JOptionPane
              .showInputDialog(optionMenu, "number of cand polyominos",
                               cont.minNumCand + "-" + cont.maxNumCand);
          if (s == null) {
            return;
          }
          try {
            String[] ss = s.split("-");
            int mn = Integer.valueOf(ss[0]);
            int mx = Integer.valueOf(ss[1]);
            if (mx <= 0 || mn <= 0 || mn > mx) {
              throw new Exception();
            }
            cont.setMinNumCand(mn);
            cont.setMaxNumCand(mx);
          } catch (Exception ex) {
            JOptionPane.showMessageDialog(numCover, "number must be integer > 0.");
          }
        }
      });
      validCellDepth.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String
              s =
              JOptionPane
                  .showInputDialog(optionMenu, "max depth of insertion", "" + cont.validCellDepth);
          if (s == null) {
            return;
          }
          try {
            int i = Integer.valueOf(s);
            if (i <= 0) {
              throw new Exception();
            }
            cont.setValidCellDepth(i);
          } catch (Exception ex) {
            JOptionPane.showMessageDialog(numCover, "number must be integer > 0.");
          }
        }
      });

      runMenu = new JMenu("run");
      this.add(runMenu);
      JMenuItem run = new JMenuItem("run");
      runMenu.add(run);
      run.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          cont.run(myself);
        }
      });
    }


  }
}
