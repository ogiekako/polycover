package ui;

public interface ProgressMonitor {

  void setValue(int n);

  public static ProgressMonitor DO_NOTHING = new ProgressMonitor() {
    @Override
    public void setValue(int n) {
      // Do nothing.
    }
  };
}
