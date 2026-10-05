package memecoinclash;

final class GameSettings {
  final int agents, obstacles, energy, maxTurns, speed;
  final boolean alliances, chaos, neon;

  GameSettings(
      int agents,
      int obstacles,
      int energy,
      int maxTurns,
      int speed,
      boolean alliances,
      boolean chaos,
      boolean neon) {
    if (agents < 2
        || agents > 8
        || obstacles < 0
        || obstacles > 70
        || energy < 10
        || energy > 50
        || maxTurns < 50
        || maxTurns > 500
        || speed < 100
        || speed > 1200) {
      throw new IllegalArgumentException("Réglages hors limites");
    }
    this.agents = agents;
    this.obstacles = obstacles;
    this.energy = energy;
    this.maxTurns = maxTurns;
    this.speed = speed;
    this.alliances = alliances;
    this.chaos = chaos;
    this.neon = neon;
  }

  static GameSettings defaults() {
    return new GameSettings(5, 22, 24, 250, 450, true, false, false);
  }
}
