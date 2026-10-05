package memecoinclash;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/** Renders the simulation without changing its state. */
final class BoardPanel extends JPanel {
  private final EnumMap<Team, BufferedImage> logos = new EnumMap<>(Team.class);
  private final Color canvas = new Color(12, 18, 35);
  private Simulation game;

  BoardPanel(Simulation game) {
    this.game = game;
    setBackground(canvas);
    for (Team team : Team.values()) {
      String file = team.name.toLowerCase(Locale.ROOT) + ".png";
      try {
        var resource = BoardPanel.class.getResourceAsStream("/assets/" + file);
        logos.put(
            team,
            resource != null ? ImageIO.read(resource) : ImageIO.read(new File("assets", file)));
      } catch (IOException ignored) {
        /* Initiales de secours si un fichier manque. */
      }
    }
  }

  @Override
  protected void paintComponent(Graphics graphics) {
    super.paintComponent(graphics);
    Graphics2D g = (Graphics2D) graphics.create();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    int cell =
        Math.max(
            1,
            Math.min((getWidth() - 48) / Simulation.WIDTH, (getHeight() - 82) / Simulation.HEIGHT));
    int left = (getWidth() - cell * Simulation.WIDTH) / 2;
    int top = (getHeight() - cell * Simulation.HEIGHT) / 2 + 12;
    g.setColor(new Color(30, 42, 67));
    g.fillRoundRect(
        left - 12, top - 12, cell * Simulation.WIDTH + 24, cell * Simulation.HEIGHT + 24, 20, 20);
    for (int y = 0; y < Simulation.HEIGHT; y++)
      for (int x = 0; x < Simulation.WIDTH; x++) {
        int px = left + x * cell, py = top + y * cell;
        Team owner = game.safeOwner(x, y);
        Color ground =
            owner == null
                ? (game.settings.neon
                    ? ((x + y) % 2 == 0 ? new Color(48, 24, 73) : new Color(36, 23, 62))
                    : ((x + y) % 2 == 0 ? new Color(26, 39, 62) : new Color(23, 35, 57)))
                : blend(owner.color, canvas, game.settings.neon ? .48f : .27f);
        g.setColor(ground);
        g.fillRect(px, py, cell, cell);
        g.setColor(new Color(255, 255, 255, 15));
        g.drawRect(px, py, cell, cell);
        if (game.obstacleAt(x, y)) {
          g.setColor(new Color(9, 14, 27));
          g.fillRoundRect(px + 5, py + 5, cell - 10, cell - 10, 8, 8);
          g.setColor(new Color(93, 111, 141));
          g.setStroke(new BasicStroke(2));
          g.drawLine(px + 10, py + 10, px + cell - 10, py + cell - 10);
          g.drawLine(px + cell - 10, py + 10, px + 10, py + cell - 10);
        }
      }
    for (Team t : Team.values()) drawZoneLabel(g, t, cell, left, top);
    for (Team t : Team.values()) drawAgent(g, game.master(t), cell, left, top, true);
    for (Agent a : game.agents()) drawAgent(g, a, cell, left, top, false);
    g.setFont(new Font("SansSerif", Font.BOLD, 11));
    g.setColor(new Color(150, 169, 199));
    g.drawString("ARÈNE DES MÈMES  /  4 FACTIONS", left, top - 19);
    g.dispose();
  }

  private Color blend(Color a, Color b, float amount) {
    return new Color(
        (int) (a.getRed() * amount + b.getRed() * (1 - amount)),
        (int) (a.getGreen() * amount + b.getGreen() * (1 - amount)),
        (int) (a.getBlue() * amount + b.getBlue() * (1 - amount)));
  }

  private void drawZoneLabel(Graphics2D g, Team t, int cell, int left, int top) {
    int x = (t == Team.DOGE || t == Team.PEPE) ? 0 : Simulation.WIDTH - 3;
    int y = (t == Team.DOGE || t == Team.SHIBA) ? 0 : Simulation.HEIGHT - 3;
    g.setColor(new Color(255, 255, 255, 115));
    g.setFont(new Font("SansSerif", Font.BOLD, Math.max(9, cell / 4)));
    g.drawString(
        t.name.toUpperCase(Locale.ROOT),
        left + x * cell + 5,
        top + y * cell + (y == 0 ? 3 * cell - 7 : cell - 7));
  }

  private void drawAgent(Graphics2D g, Agent a, int cell, int left, int top, boolean master) {
    int px = left + a.x * cell, py = top + a.y * cell;
    int inset = master ? 2 : 4, diameter = cell - inset * 2;
    g.setColor(new Color(0, 0, 0, 90));
    g.fillOval(px + inset + 2, py + inset + 3, diameter, diameter);
    g.setColor(
        a.exhausted
            ? new Color(75, 82, 96)
            : master ? a.team.color : blend(a.team.color, Color.WHITE, .58f));
    g.fillOval(px + inset, py + inset, diameter, diameter);
    g.setColor(master ? Color.WHITE : new Color(255, 255, 255, 180));
    g.setStroke(new BasicStroke(master ? 2.5f : 1.2f));
    g.drawOval(px + inset, py + inset, diameter, diameter);
    if (a.exhausted) drawCentered(g, "×", px, py, cell, Color.WHITE);
    else {
      BufferedImage logo = logos.get(a.team);
      if (logo != null) {
        int fit = Math.max(1, (int) (diameter * (master ? .76 : .72)));
        double scale = Math.min((double) fit / logo.getWidth(), (double) fit / logo.getHeight());
        int w = Math.max(1, (int) (logo.getWidth() * scale)),
            h = Math.max(1, (int) (logo.getHeight() * scale));
        g.drawImage(logo, px + (cell - w) / 2, py + (cell - h) / 2, w, h, null);
      } else drawCentered(g, a.team.symbol, px, py, cell, Color.WHITE);
    }
    if (master) {
      g.setColor(new Color(255, 255, 255, 230));
      g.fillOval(px + cell - 12, py + 1, 11, 11);
      g.setColor(canvas);
      g.setFont(new Font("SansSerif", Font.BOLD, 8));
      g.drawString("M", px + cell - 10, py + 10);
    } else if (!a.exhausted) {
      g.setColor(new Color(8, 13, 25));
      g.fillRoundRect(px + 4, py + cell - 5, cell - 8, 4, 4, 4);
      g.setColor(a.energy < 5 ? new Color(255, 92, 99) : new Color(100, 232, 155));
      g.fillRoundRect(px + 4, py + cell - 5, (cell - 8) * a.energy / game.settings.energy, 4, 4, 4);
    }
  }

  private void drawCentered(Graphics2D g, String value, int x, int y, int size, Color color) {
    g.setFont(new Font("SansSerif", Font.BOLD, Math.max(12, size / 2)));
    FontMetrics fm = g.getFontMetrics();
    g.setColor(color);
    g.drawString(
        value,
        x + (size - fm.stringWidth(value)) / 2,
        y + (size + fm.getAscent() - fm.getDescent()) / 2);
  }

  void setGame(Simulation game) {
    this.game = game;
    repaint();
  }
}
