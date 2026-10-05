package memecoinclash;

import java.awt.*;
import javax.swing.*;

/** Builds a new configuration; Cancel returns null without changing the current game. */
final class SettingsDialog {
  private SettingsDialog() {}

  static GameSettings show(JFrame owner, GameSettings settings) {
    JSpinner agents = spinner(settings.agents, 2, 8, 1);
    JSpinner obstacles = spinner(settings.obstacles, 0, 70, 1);
    JSpinner energy = spinner(settings.energy, 10, 50, 1);
    JSpinner turns = spinner(settings.maxTurns, 50, 500, 25);
    JSlider speed = new JSlider(100, 1200, settings.speed);
    speed.setMajorTickSpacing(200);
    speed.setPaintTicks(true);
    JLabel speedValue = new JLabel(settings.speed + " ms / tour");
    speed.addChangeListener(e -> speedValue.setText(speed.getValue() + " ms / tour"));
    JCheckBox alliances = new JCheckBox("Alliances entre équipes", settings.alliances);
    JCheckBox chaos = new JCheckBox("Airdrop surprise tous les 10 tours", settings.chaos);
    JCheckBox neon = new JCheckBox("Arène néon", settings.neon);
    JPanel form = new JPanel(new GridBagLayout());
    form.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(6, 7, 6, 7);
    c.anchor = GridBagConstraints.WEST;
    addSetting(form, c, 0, "Mèmes par équipe", agents);
    addSetting(form, c, 1, "Obstacles dans l'arène", obstacles);
    addSetting(form, c, 2, "Énergie des mèmes", energy);
    addSetting(form, c, 3, "Tours maximum", turns);
    JPanel speedRow = new JPanel(new BorderLayout(8, 0));
    speedRow.add(speed, BorderLayout.CENTER);
    speedRow.add(speedValue, BorderLayout.EAST);
    addSetting(form, c, 4, "Vitesse de la folie", speedRow);
    c.gridx = 0;
    c.gridwidth = 2;
    c.gridy = 5;
    form.add(alliances, c);
    c.gridy = 6;
    form.add(chaos, c);
    c.gridy = 7;
    form.add(neon, c);
    c.gridy = 8;
    form.add(new JLabel("Appliquer lance une nouvelle partie."), c);
    int result =
        JOptionPane.showConfirmDialog(
            owner,
            form,
            "Personnaliser la partie",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
    if (result == JOptionPane.OK_OPTION) {
      return new GameSettings(
          (int) agents.getValue(),
          (int) obstacles.getValue(),
          (int) energy.getValue(),
          (int) turns.getValue(),
          speed.getValue(),
          alliances.isSelected(),
          chaos.isSelected(),
          neon.isSelected());
    }
    return null;
  }

  private static JSpinner spinner(int value, int min, int max, int step) {
    return new JSpinner(new SpinnerNumberModel(value, min, max, step));
  }

  private static void addSetting(
      JPanel panel, GridBagConstraints c, int row, String label, JComponent control) {
    c.gridy = row;
    c.gridx = 0;
    c.gridwidth = 1;
    panel.add(new JLabel(label), c);
    c.gridx = 1;
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    panel.add(control, c);
    c.weightx = 0;
    c.fill = GridBagConstraints.NONE;
  }
}
