package memecoinclash;

import java.util.HashSet;
import java.util.Set;

abstract class Agent {
  static int instanceCount;
  final Team team;
  int x, y, energy = 24, lastDx, lastDy;
  boolean exhausted;
  final Set<Integer> messages = new HashSet<>();

  Agent(Team team, int x, int y) {
    this.team = team;
    this.x = x;
    this.y = y;
    instanceCount++;
  }

  abstract void move(Simulation game);

  void travel(Simulation game, int dx, int dy, int distance) {
    if (exhausted) return;
    if (energy * 5 < game.settings.energy && !game.isSafe(team, x, y)) {
      int[] home = game.directionHome(this);
      dx = home[0];
      dy = home[1];
      distance = 1;
    }
    if (dx == 0 && dy == 0) {
      recover(game);
      return;
    }
    if (dx == lastDx && dy == lastDy && game.blockedAhead(this, dx, dy)) {
      dx = -dx;
      dy = -dy;
    }
    lastDx = dx;
    lastDy = dy;
    for (int step = 0; step < distance; step++) {
      int nx = x + dx, ny = y + dy;
      if (!game.inBounds(nx, ny) || !game.mayEnter(team, nx, ny) || game.obstacleAt(nx, ny)) {
        spend(game, distance - step);
        break;
      }
      Agent other = game.agentAt(nx, ny);
      if (other != null) {
        game.encounter(this, other);
        spend(game, distance - step);
        break;
      }
      x = nx;
      y = ny;
      if (!game.isSafe(team, x, y)) spend(game, 1);
      game.deliver(this);
      if (exhausted) break;
    }
    recover(game);
  }

  private void spend(Simulation game, int amount) {
    if (game.isSafe(team, x, y)) return;
    energy = Math.max(0, energy - amount);
    if (energy == 0) {
      exhausted = true;
      messages.clear();
      game.log(team.name + " : un agent épuisé devient un obstacle.");
    }
  }

  private void recover(Simulation game) {
    if (game.isSafe(team, x, y)) energy = Math.min(game.settings.energy, energy + 5);
    game.deliver(this);
  }
}
