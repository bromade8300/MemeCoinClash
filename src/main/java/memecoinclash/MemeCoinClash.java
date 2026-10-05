package memecoinclash;

import javax.swing.SwingUtilities;

public class MemeCoinClash {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new GameWindow().setVisible(true));
  }
}
