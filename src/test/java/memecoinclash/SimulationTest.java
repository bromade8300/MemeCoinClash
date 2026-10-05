package memecoinclash;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

/** Dependency-free checks for the rules required by the assignment. */
public final class SimulationTest {
  private static int checks;

  public static void main(String[] args) {
    RandomSource.get().setSeed(20261001L);
    GameMap map = new GameMap();
    require(map.neighbors(0, 0).size() == 3, "corner neighbors");
    require(map.neighbors(0, 5).size() == 5, "edge neighbors");
    require(map.neighbors(5, 5).size() == 8, "interior neighbors");
    require(map.directionHome(Team.BONK, 0, 0).equals(new Position(1, 1)), "home direction");
    require(DogeMaster.get() == DogeMaster.get(), "Doge singleton");
    require(ShibaMaster.get() == ShibaMaster.get(), "Shiba singleton");
    require(PepeMaster.get() == PepeMaster.get(), "Pepe singleton");
    require(BonkMaster.get() == BonkMaster.get(), "Bonk singleton");

    for (int population : new int[] {2, 5, 8}) {
      GameSettings settings = new GameSettings(population, 70, 10, 50, 100, true, false, false);
      Simulation simulation = new Simulation(settings);
      require(simulation.agents().size() == 4 * population, "population size");
      Set<Integer> messages = new HashSet<>();
      for (Agent agent : simulation.agents()) {
        require(agent.energy == 10, "starting energy");
        require(simulation.safeOwner(agent.x, agent.y) == agent.team, "initial safe zone");
        messages.addAll(agent.messages);
      }
      require(messages.size() == Simulation.MESSAGE_COUNT, "all messages initially exist");
      int obstacles = 0;
      for (int y = 0; y < GameMap.HEIGHT; y++)
        for (int x = 0; x < GameMap.WIDTH; x++) {
          if (simulation.obstacleAt(x, y)) obstacles++;
        }
      require(obstacles == 70, "obstacle count");
      checkOccupancy(simulation);
      while (!simulation.finished) {
        simulation.step();
        checkOccupancy(simulation);
      }
      require(simulation.turn <= 50, "turn limit");
    }

    Simulation rules = new Simulation(GameSettings.defaults());
    Agent first = rules.agents().get(0);
    Agent second = rules.agents().get(1);
    first.messages.clear();
    second.messages.clear();
    first.messages.add(0);
    second.messages.add(1);
    rules.encounter(first, second);
    require(first.messages.equals(Set.of(0, 1)), "same-team union for first agent");
    require(second.messages.equals(Set.of(0, 1)), "same-team union for second agent");
    Master master = rules.master(first.team);
    first.x = master.x;
    first.y = master.y + 1;
    rules.deliver(first);
    require(rules.score(first.team) == 2, "only delivered messages contribute to score");
    first.x = 5;
    first.y = 5;
    first.energy = 1;
    first.travel(rules, 1, 0, 1);
    require(first.exhausted && first.messages.isEmpty(), "exhaustion clears messages");

    BoardPanel board = new BoardPanel(new Simulation(GameSettings.defaults()));
    board.setSize(900, 650);
    BufferedImage image = new BufferedImage(900, 650, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = image.createGraphics();
    board.paint(graphics);
    graphics.dispose();
    require(image.getRGB(450, 325) != 0, "board renders");
    testEncounters();
    testTravel();
    testEndConditions();
    require(replay().equals(replay()), "same seed reproduces the simulation");
    System.out.println("SimulationTest: OK (" + checks + " assertions)");
  }

  private static GameSettings settings(boolean alliances) {
    return new GameSettings(2, 0, 24, 50, 100, alliances, false, false);
  }

  private static void checkOccupancy(Simulation game) {
    Set<Position> occupied = new HashSet<>();
    for (Team team : Team.values()) {
      Master master = game.master(team);
      require(occupied.add(new Position(master.x, master.y)), "unique master position");
    }
    for (Agent agent : game.agents()) {
      require(game.inBounds(agent.x, agent.y), "agents stay in bounds");
      require(!game.obstacleAt(agent.x, agent.y), "no agent overlaps terrain");
      require(game.mayEnter(agent.team, agent.x, agent.y), "reserved safe zones");
      require(occupied.add(new Position(agent.x, agent.y)), "one occupant per tile");
      require(agent.energy >= 0 && agent.energy <= game.settings.energy, "energy bounds");
      require(!agent.exhausted || agent.messages.isEmpty(), "exhausted agents lose messages");
    }
  }

  private static void testEncounters() {
    Simulation game = new Simulation(settings(true));
    Agent doge = game.agents().get(0), shiba = game.agents().get(2);
    doge.messages.clear();
    shiba.messages.clear();
    doge.messages.addAll(Set.of(0, 1, 2, 3, 4, 5));
    shiba.messages.addAll(Set.of(6, 7, 8, 9, 10, 11));
    game.encounter(doge, shiba);
    require(doge.messages.size() >= 7 && doge.messages.size() <= 8, "random allied copy count");
    require(shiba.messages.size() >= 7 && shiba.messages.size() <= 8, "random reciprocal copy count");
    require(doge.messages.containsAll(Set.of(0, 1, 2, 3, 4, 5)), "allied giver keeps messages");
    require(shiba.messages.containsAll(Set.of(6, 7, 8, 9, 10, 11)), "allied partner keeps messages");

    game = new Simulation(settings(false));
    doge = game.agents().get(0);
    shiba = game.agents().get(2);
    doge.messages.clear();
    shiba.messages.clear();
    doge.messages.addAll(Set.of(0, 1, 2, 3, 4, 5));
    shiba.messages.addAll(Set.of(6, 7, 8, 9, 10, 11));
    game.encounter(doge, shiba);
    require(doge.messages.size() + shiba.messages.size() == 12, "combat transfers without copying");
    Set<Integer> union = new HashSet<>(doge.messages);
    union.addAll(shiba.messages);
    require(union.size() == 12, "combat preserves distinct messages");
    require(Math.abs(doge.messages.size() - 6) >= 1
        && Math.abs(doge.messages.size() - 6) <= 2, "combat transfers one or two messages");
    shiba.exhausted = true;
    Set<Integer> previous = new HashSet<>(doge.messages);
    game.encounter(doge, shiba);
    require(doge.messages.equals(previous), "exhausted agent cannot share");
  }

  private static void testTravel() {
    Simulation game = new Simulation(settings(true));
    Agent doge = game.agents().get(0), shiba = game.agents().get(2);
    doge.x = 5;
    doge.y = 5;
    shiba.x = 6;
    shiba.y = 5;
    game.deliver(doge);
    require(game.score(Team.DOGE) == 0, "distant messages do not count");
    doge.travel(game, 1, 0, 3);
    require(doge.x == 5 && doge.y == 5, "meeting stops movement before occupied tile");
    require(doge.energy == 21, "blocked remaining distance consumes energy");
    doge.energy = 4;
    doge.travel(game, 1, 0, 3);
    require(doge.x == 4 && doge.y == 4, "low energy overrides direction towards home");
    require(doge.energy == 3, "return moves one tile");
    doge.x = 1;
    doge.y = 1;
    doge.travel(game, 0, 0, 1);
    require(doge.energy == 8, "safe zone recovers five points");
    int score = game.score(Team.DOGE);
    game.deliver(doge);
    require(game.score(Team.DOGE) == score, "delivery deduplicates messages");
    doge.energy = 23;
    doge.travel(game, 0, 0, 1);
    require(doge.energy == 24, "recovery cannot exceed maximum");
  }

  private static void testEndConditions() {
    Simulation game = new Simulation(settings(true));
    Agent carrier = game.agents().get(0);
    for (int i = 0; i < Simulation.MESSAGE_COUNT; i++) carrier.messages.add(i);
    carrier.x = 1;
    carrier.y = 1;
    game.deliver(carrier);
    game.step();
    require(game.finished && game.result.startsWith("Doge gagne"), "complete collection wins");
    int turn = game.turn;
    game.step();
    require(game.turn == turn, "finished game stops stepping");
    game.reset();
    require(!game.finished && game.turn == 0 && game.score(Team.DOGE) == 0, "reset clears game state");
    for (Agent agent : game.agents()) {
      agent.exhausted = true;
      agent.energy = 0;
      agent.messages.clear();
    }
    while (!game.finished) game.step();
    require(game.turn == 50 && game.result.startsWith("Match nul"), "equal scores draw at turn limit");
  }

  private static String replay() {
    RandomSource.get().setSeed(73L);
    Simulation game = new Simulation(settings(true));
    for (int i = 0; i < 20; i++) game.step();
    StringBuilder state = new StringBuilder();
    for (Agent agent : game.agents())
      state.append(agent.team).append(':').append(agent.x).append(',').append(agent.y)
          .append(',').append(agent.energy).append(',').append(agent.exhausted)
          .append(',').append(new java.util.TreeSet<>(agent.messages)).append(';');
    for (Team team : Team.values()) state.append(game.score(team)).append(';');
    return state.toString();
  }

  private static void require(boolean condition, String message) {
    checks++;
    if (!condition) throw new AssertionError(message);
  }
}
