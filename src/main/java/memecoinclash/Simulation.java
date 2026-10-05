package memecoinclash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class Simulation {
  static final int WIDTH = GameMap.WIDTH, HEIGHT = GameMap.HEIGHT, MESSAGE_COUNT = 24;
  final GameSettings settings;
  private final List<Agent> agents = new ArrayList<>();
  private final EnumMap<Team, Master> masters = new EnumMap<>(Team.class);
  private final GameMap map = new GameMap();
  private final List<String> events = new ArrayList<>();
  int turn;
  boolean finished;
  String result = "";

  Simulation() {
    this(GameSettings.defaults());
  }

  Simulation(GameSettings settings) {
    this.settings = settings;
    reset();
  }

  void reset() {
    agents.clear();
    events.clear();
    turn = 0;
    finished = false;
    result = "";
    map.clear();
    masters.put(Team.DOGE, DogeMaster.get());
    masters.put(Team.SHIBA, ShibaMaster.get());
    masters.put(Team.PEPE, PepeMaster.get());
    masters.put(Team.BONK, BonkMaster.get());
    for (Team team : Team.values()) {
      Master master = masters.get(team);
      master.clear();
      master.x = team == Team.DOGE || team == Team.PEPE ? 0 : WIDTH - 1;
      master.y = team == Team.DOGE || team == Team.SHIBA ? 0 : HEIGHT - 1;
    }
    map.placeRandomObstacles(settings.obstacles);
    for (Team team : Team.values()) {
      for (int i = 0; i < settings.agents; i++) {
        int[] pos = freeInSafeZone(team);
        Agent agent =
            switch (team) {
              case DOGE -> new DogeAgent(pos[0], pos[1]);
              case SHIBA -> new ShibaAgent(pos[0], pos[1]);
              case PEPE -> new PepeAgent(pos[0], pos[1]);
              case BONK -> new BonkAgent(pos[0], pos[1]);
            };
        agent.energy = settings.energy;
        // Every original message exists even with the minimum of two agents per team.
        for (int message = team.ordinal() * 6 + i;
            message < team.ordinal() * 6 + 6;
            message += settings.agents) {
          agent.messages.add(message);
        }
        agents.add(agent);
      }
    }
    log("Nouvelle partie : 4 équipes, " + MESSAGE_COUNT + " messages à collecter.");
  }

  private int[] freeInSafeZone(Team team) {
    int ox = team == Team.DOGE || team == Team.PEPE ? 0 : WIDTH - 3;
    int oy = team == Team.DOGE || team == Team.SHIBA ? 0 : HEIGHT - 3;
    for (int y = oy; y < oy + 3; y++)
      for (int x = ox; x < ox + 3; x++) if (agentAt(x, y) == null) return new int[] {x, y};
    throw new IllegalStateException("Zone sécurisée pleine");
  }

  void step() {
    if (finished) return;
    turn++;
    List<Agent> order = new ArrayList<>(agents);
    RandomSource.get().shuffle(order);
    for (Agent a : order) {
      if (!a.exhausted) a.move(this);
      if (finishIfComplete()) return;
    }
    if (settings.chaos && turn % 10 == 0) {
      List<Agent> active = new ArrayList<>();
      for (Agent a : agents) if (!a.exhausted) active.add(a);
      if (!active.isEmpty()) {
        Agent lucky = active.get(RandomSource.get().nextInt(active.size()));
        lucky.messages.add(RandomSource.get().nextInt(MESSAGE_COUNT));
        log("Airdrop surprise pour " + lucky.team.name + " !");
      }
    }
    if (!finished && turn >= settings.maxTurns) {
      int best = Arrays.stream(Team.values()).mapToInt(this::score).max().orElse(0);
      List<String> winners = new ArrayList<>();
      for (Team team : Team.values()) if (score(team) == best) winners.add(team.name);
      result =
          winners.size() == 1
              ? winners.get(0) + " gagne avec " + best + " messages."
              : "Match nul (" + best + " messages).";
      finished = true;
    }
    if (finished) log(result);
  }

  private boolean finishIfComplete() {
    for (Team team : Team.values()) {
      if (score(team) == MESSAGE_COUNT) {
        finished = true;
        result = team.name + " gagne : tous les messages livrés.";
        log(result);
        return true;
      }
    }
    return false;
  }

  int score(Team t) {
    return masters.get(t).messages.size();
  }

  Master master(Team t) {
    return masters.get(t);
  }

  List<Agent> agents() {
    return Collections.unmodifiableList(agents);
  }

  List<String> events() {
    return Collections.unmodifiableList(events);
  }

  void log(String message) {
    events.add("Tour " + turn + " · " + message);
    if (events.size() > 120) events.remove(0);
  }

  GameMap map() {
    return map;
  }

  boolean inBounds(int x, int y) {
    return map.inBounds(x, y);
  }

  boolean obstacleAt(int x, int y) {
    return map.obstacleAt(x, y);
  }

  Team safeOwner(int x, int y) {
    return map.safeOwner(x, y);
  }

  boolean isSafe(Team team, int x, int y) {
    return map.isSafe(team, x, y);
  }

  boolean mayEnter(Team team, int x, int y) {
    return map.mayEnter(team, x, y);
  }

  Agent agentAt(int x, int y) {
    for (Master m : masters.values()) if (m.x == x && m.y == y) return m;
    for (Agent a : agents) if (a.x == x && a.y == y) return a;
    return null;
  }

  boolean blockedAhead(Agent a, int dx, int dy) {
    int x = a.x + dx, y = a.y + dy;
    return !inBounds(x, y) || !mayEnter(a.team, x, y) || obstacleAt(x, y) || agentAt(x, y) != null;
  }

  int[] directionHome(Agent a) {
    Position direction = map.directionHome(a.team, a.x, a.y);
    return new int[] {direction.x(), direction.y()};
  }

  int[] randomDirection() {
    int[][] directions = {{-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1}, {0, 1}, {1, 1}};
    return directions[RandomSource.get().nextInt(directions.length)];
  }

  void deliver(Agent a) {
    Master m = masters.get(a.team);
    if (Math.max(Math.abs(a.x - m.x), Math.abs(a.y - m.y)) > 1) return;
    Set<Integer> received = new HashSet<>(a.messages);
    received.removeAll(m.messages);
    m.messages.addAll(a.messages);
    if (!received.isEmpty())
      log(a.team.name + " livre " + messageLetters(received) + " à son Maître.");
  }

  void encounter(Agent a, Agent b) {
    if (b instanceof Master) {
      if (a.team == b.team) deliver(a);
      return;
    }
    if (b.exhausted) return;
    if (a.team == b.team) {
      Set<Integer> learnedByA = new HashSet<>(b.messages);
      learnedByA.removeAll(a.messages);
      Set<Integer> learnedByB = new HashSet<>(a.messages);
      learnedByB.removeAll(b.messages);
      a.messages.addAll(b.messages);
      b.messages.addAll(a.messages);
      log(
          a.team.name
              + " partage : "
              + messageLetters(learnedByA)
              + " / "
              + messageLetters(learnedByB)
              + ".");
    } else if (settings.alliances && a.team.alliance == b.team.alliance) {
      List<Integer> toB = shareSome(a, b);
      List<Integer> toA = shareSome(b, a);
      log(
          a.team.name
              + " → "
              + b.team.name
              + " : "
              + messageLetters(toB)
              + " ; retour : "
              + messageLetters(toA)
              + ".");
    } else {
      Agent winner = RandomSource.get().nextBoolean() ? a : b;
      Agent loser = winner == a ? b : a;
      List<Integer> available = new ArrayList<>(loser.messages);
      available.removeAll(winner.messages);
      RandomSource.get().shuffle(available);
      int take = Math.min(available.size(), 1 + RandomSource.get().nextInt(2));
      List<Integer> transferred = new ArrayList<>();
      for (int i = 0; i < take; i++) {
        int msg = available.get(i);
        loser.messages.remove(msg);
        winner.messages.add(msg);
        transferred.add(msg);
      }
      log(
          winner.team.name
              + " bat "
              + loser.team.name
              + " et prend "
              + messageLetters(transferred)
              + ".");
    }
  }

  private List<Integer> shareSome(Agent giver, Agent receiver) {
    List<Integer> unseen = new ArrayList<>(giver.messages);
    unseen.removeAll(receiver.messages);
    RandomSource.get().shuffle(unseen);
    List<Integer> shared = new ArrayList<>();
    int count = Math.min(unseen.size(), 1 + RandomSource.get().nextInt(2));
    for (int i = 0; i < count; i++) {
      int message = unseen.get(i);
      receiver.messages.add(message);
      shared.add(message);
    }
    return shared;
  }

  private String messageLetters(java.util.Collection<Integer> messages) {
    if (messages.isEmpty()) return "aucun message";
    List<Integer> ordered = new ArrayList<>(messages);
    Collections.sort(ordered);
    StringBuilder letters = new StringBuilder();
    for (int message : ordered) {
      if (!letters.isEmpty()) letters.append(", ");
      letters.append((char) ('A' + message));
    }
    return letters.toString();
  }
}
