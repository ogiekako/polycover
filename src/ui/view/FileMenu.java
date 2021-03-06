package ui.view;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.*;

import main.Poly;
import main.Prop;
import ui.Cont;
import ui.DialogShower;
import util.FileUtil;

class FileMenu extends JMenu {

  JFrame parent;
  Cont cont;

  FileMenu(JFrame parent, final Cont cont) {
    super("file");
    this.parent = parent;
    this.cont = cont;

    addMenuItem("new", new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        nw();
      }
    });
    addMenuItem("load", new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String lastDirOrNull = Prop.get("lastloadpath");
        JFileChooser chooser = new JFileChooser(lastDirOrNull);
        chooser.showOpenDialog(FileMenu.this);
        File file = chooser.getSelectedFile();
        if (file == null) {
          return;
        }
        Prop.set("lastloadpath", file.getPath());
        try {
          cont.load(file);
        } catch (IOException e1) {
          DialogShower.message(FileMenu.this, e1.getMessage());
        }
      }
    });
    addMenuItem("save", new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String lastDirOrNull = Prop.get("lastsavepath");
        JFileChooser chooser = new JFileChooser(lastDirOrNull);
        chooser.showSaveDialog(FileMenu.this);
        File file = chooser.getSelectedFile();
        if (file == null) {
          return ;
        }
        Prop.set("lastsavepath", file.getPath());
        try {
          cont.save(file);
        } catch (IOException e1) {
          DialogShower.message(FileMenu.this, e1.getMessage());
        }
      }
    });
    addMenuItem("save cand", new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String probFileOrNull = Prop.get("lastprobfile");
        JFileChooser chooser;
        if (probFileOrNull != null) {
          String ansFile = probFileOrNull.replace("problem", "ans");
          String[] ss = ansFile.split("/");
          chooser = new JFileChooser(ansFile.replace("/" + ss[ss.length-1], ""));
          chooser.setSelectedFile(new File(ansFile));
        } else {
          chooser = new JFileChooser();
        }
        chooser.showSaveDialog(FileMenu.this);
        File file = chooser.getSelectedFile();
        if (file == null) {
          return ;
        }
        try {
          FileUtil.savePoly(cont.cand.getPoly(), file);
        } catch (IOException e1) {
          DialogShower.message(FileMenu.this, e1.getMessage());
        }
      }
    });
  }

  private void addMenuItem(String name, ActionListener listener) {
    JMenuItem menuItem = new JMenuItem(name);
    menuItem.addActionListener(listener);
    this.add(menuItem);
  }

  private void nw() {
    final JDialog sizeDialog = new JDialog(parent);
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
          cont.setCand(new Poly(new boolean[n][n]));
          sizeDialog.dispose();
        } catch (Exception ex) {
          JOptionPane.showMessageDialog(panel, "specify integer > 0.");
        }
      }
    });
  }
}
