package ui.view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;

import javax.swing.*;

import ai.AI;
import main.PolyArray;
import ui.Cont;
import ui.DialogShower;
import util.Debug;

public class AIDialog extends JDialog {

  private final Cont cont;
  private final Component where;

  private final AnnotatedTextField maxIter;
  private final JCheckBox rotSym;
  private final JCheckBox revRotSym;
  private final JCheckBox allowUnconnected;
  private final JCheckBox allowHole;
  JPanel panel;
  AI.Option aiOption = new AI.Option();

  JButton submitBtn = new JButton("submit");

  AIDialog(Cont cont, JComponent where) {
    this.cont = cont;
    this.where = where;

    setLocationRelativeTo(where);
    setTitle("AI option");
    setResizable(true);
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    panel = new JPanel();
    panel.add(maxIter = new AnnotatedTextField("#iterations", "100"));
    panel.add(rotSym = new JCheckBox("rot sym"));
    panel.add(revRotSym = new JCheckBox("rev rot sym"));
    panel.add(allowUnconnected = new JCheckBox("allow unconnected"));
    panel.add(allowHole = new JCheckBox("allow hole(s)"));
    panel.add(submitBtn);
    panel.setLayout(new GridLayout(panel.getComponentCount(), 1));
    this.add(panel);
    this.pack();
    setVisible(true);

    submitBtn.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        submit();
      }
    });
  }

  private void submit() {
    Debug.debug("submit");
    aiOption.maxIter = Integer.parseInt(maxIter.getText());
    aiOption.allowHole = allowHole.isSelected();
    aiOption.allowUnconnected = allowUnconnected.isSelected();
    aiOption.revRotSym = revRotSym.isSelected();
    aiOption.rotSym = rotSym.isSelected();
    cont.setAiOption(aiOption);
    cont.ai(new Cont.AICallback() {

      @Override
      public void done(boolean aborted, AI.Result best) {
        Debug.debug(aborted, "A");
        String msg;
        if (best.maxAllowableDepth == AI.INF) {
          Debug.debug(aborted, "B");
          msg = "maybe a solution.";
        } else {
          Debug.debug(aborted, "C");
          msg = String.format("depth = %d.", best.maxAllowableDepth);
        }

        Debug.debug(aborted, msg);
        DialogShower.message(where, msg);
      }
    });
    this.setVisible(false);
    this.dispose();
  }

  private static class AnnotatedTextField extends JPanel {

    final JTextField field;

    AnnotatedTextField(String name, String defaultText) {
      FlowLayout layout = new FlowLayout();
      layout.setAlignment(FlowLayout.LEFT);
      setLayout(layout);
      setLayout(new GridLayout(1, 2));
      add(new JLabel(name + ":"));
      add(field = new JTextField(defaultText));
    }

    String getText() {
      return field.getText();
    }
  }


  public static void main(String[] args) {
    final Cont cont = new Cont();
    cont.setProblem(PolyArray.load(new Scanner("3 3\n"
                                               + "###\n"
                                               + "..#\n"
                                               + "..#")));
    cont.setCand(PolyArray.load(new Scanner("5 5\n"
                                            + ".#...\n"
                                            + "###..\n"
                                            + "..##.\n"
                                            + "...##\n"
                                            + "...#.\n")));
    cont.addView(new View() {
      @Override
      public void update() {
        Debug.debug(cont.aiOption);
        Debug.debug(cont.cand);
        Debug.debug(cont.maxAllowedDepth);
      }
    });
    AIDialog dialog = new AIDialog(cont, null);
  }
}
