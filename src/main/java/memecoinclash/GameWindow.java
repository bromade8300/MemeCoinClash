package memecoinclash;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.*;

final class GameWindow extends JFrame {
  private GameSettings settings = GameSettings.defaults();
  private Simulation game = new Simulation(settings);
  private final BoardPanel board = new BoardPanel(game);
  private final JTextArea log = new JTextArea();
  private final JLabel status = new JLabel();
  private final JLabel instances = new JLabel();
  private final JButton play = new JButton("Démarrer");
  private final javax.swing.Timer timer =
      new javax.swing.Timer(settings.speed, this::automaticStep);

  GameWindow() {
    super("MemeCoin Clash");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout(10, 10));
    JPanel top = new JPanel(new BorderLayout());
    JLabel title =
        new JLabel("  MEMECOIN CLASH  ·  La chasse aux mèmes rares", SwingConstants.LEFT);
    title.setFont(new Font("SansSerif", Font.BOLD, 20));
    top.add(title, BorderLayout.NORTH);
    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
    play.addActionListener(
        e -> {
          if (timer.isRunning()) pause();
          else {
            timer.start();
            play.setText("Pause");
          }
        });
    JButton next = new JButton("Un tour");
    next.addActionListener(
        e -> {
          pause();
          game.step();
          refresh();
        });
    JButton reset = new JButton("Nouvelle partie");
    reset.addActionListener(e -> startNewGame());
    JButton options = new JButton("⚙ Réglages");
    options.addActionListener(e -> showSettings());
    buttons.add(play);
    buttons.add(next);
    buttons.add(reset);
    buttons.add(options);
    top.add(buttons, BorderLayout.SOUTH);
    add(top, BorderLayout.NORTH);
    add(board, BorderLayout.CENTER);
    log.setEditable(false);
    log.setFont(new Font("SansSerif", Font.PLAIN, 12));
    log.setLineWrap(true);
    log.setWrapStyleWord(true);
    JScrollPane side = new JScrollPane(log);
    side.setPreferredSize(new Dimension(300, 450));
    side.setBorder(BorderFactory.createTitledBorder("Rencontres et livraisons"));
    JPanel sidebar = new JPanel(new BorderLayout());
    sidebar.add(side, BorderLayout.CENTER);
    instances.setBorder(BorderFactory.createEmptyBorder(8, 10, 10, 10));
    sidebar.add(instances, BorderLayout.SOUTH);
    add(sidebar, BorderLayout.EAST);
    status.setBorder(BorderFactory.createEmptyBorder(5, 12, 10, 12));
    add(status, BorderLayout.SOUTH);
    setSize(1190, 760);
    setLocationRelativeTo(null);
    refresh();
  }

  private void showSettings() {
    pause();
    GameSettings selected = SettingsDialog.show(this, settings);
    if (selected == null) return;
    settings = selected;
    timer.setDelay(settings.speed);
    startNewGame();
  }

  private void startNewGame() {
    pause();
    game = new Simulation(settings);
    board.setGame(game);
    refresh();
  }

  private void automaticStep(ActionEvent event) {
    game.step();
    refresh();
    if (game.finished) pause();
  }

  private void pause() {
    timer.stop();
    play.setText("Démarrer");
  }

  private void refresh() {
    StringBuilder scores =
        new StringBuilder("Tour " + game.turn + "/" + settings.maxTurns + "    ");
    for (Team t : Team.values())
      scores
          .append(t.name)
          .append(" ")
          .append(game.score(t))
          .append("/")
          .append(Simulation.MESSAGE_COUNT)
          .append("    ");
    if (game.finished) scores.append("  ·  ").append(game.result);
    status.setText(scores.toString());
    instances.setText(
        "<html><b>Instances créées</b><br>Doge : "
            + DogeAgent.count
            + " · Shiba : "
            + ShibaAgent.count
            + "<br>Pepe : "
            + PepeAgent.count
            + " · Bonk : "
            + BonkAgent.count
            + "<br>Maîtres : 4 (Singleton) · Total : "
            + Agent.instanceCount
            + "</html>");
    StringBuilder history = new StringBuilder();
    List<String> events = game.events();
    for (int i = events.size() - 1; i >= Math.max(0, events.size() - 35); i--)
      history.append(events.get(i)).append("\n\n");
    log.setText(history.toString());
    log.setCaretPosition(0);
    board.repaint();
  }
}
