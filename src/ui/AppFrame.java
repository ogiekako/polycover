package ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;

import main.PolyArray;
import main.Prop;

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

    if (Prop.get("lastpath") != null) {
      cont.load(this, new File(Prop.get("lastpath")));
    }
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
      fileMenu = new JMenu("file");
      this.add(fileMenu);
      final JMenuItem nw = new JMenuItem("new");
      final JMenuItem save = new JMenuItem("save");
      final JMenuItem load = new JMenuItem("load");
      fileMenu.add(nw);
      fileMenu.add(load);
      fileMenu.add(save);
      nw.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          nw();
        }
      });
      save.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          cont.save(fileMenu);
        }
      });
      load.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String lastDirOrNull = Prop.get("lastpath");
          JFileChooser chooser = new JFileChooser(lastDirOrNull);
          chooser.showOpenDialog(fileMenu);
          File file = chooser.getSelectedFile();
          if (file == null) {
            return;
          }
          Prop.set("lastpath", file.getPath());
          cont.load(fileMenu, file);
        }
      });

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

    private void nw() {

      final JDialog sizeDialog = new JDialog(myself);
      sizeDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      sizeDialog.setVisible(true);
      sizeDialog.setSize(150, 100);
      sizeDialog.setTitle("size of cand poly");

      final JPanel panel = new JPanel();
      sizeDialog.add(panel);

      final JTextField sizeField = new JTextField("      31");
      if (!cont.cand.isNull()) {
        sizeField.setText("      " + cont.cand.getHeight());
      }
      final JButton okButton = new JButton("OK");

      panel.add(new JLabel("size:"));
      panel.add(sizeField);
      panel.add(okButton);

      okButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          try {
            int n = Integer.valueOf(sizeField.getText().trim());
            if (n <= 0) {
              throw new Exception();
            }
            cont.setCand(new PolyArray(new boolean[n][n]));
            sizeDialog.dispose();
          } catch (Exception ex) {
            JOptionPane.showMessageDialog(panel, "specify integer > 0.");
          }
        }
      });
    }
  }
}
