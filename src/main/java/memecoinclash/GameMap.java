package memecoinclash;

import java.util.ArrayList;
import java.util.List;

/** Geometry and terrain of one rectangular arena. Coordinates start at the top left. */
final class GameMap {
  static final int WIDTH = 20;
  static final int HEIGHT = 14;
  private static final int SAFE_ZONE_SIZE = 3;
  private final boolean[][] obstacles = new boolean[HEIGHT][WIDTH];

  void clear() {
    for (boolean[] row : obstacles) java.util.Arrays.fill(row, false);
  }

  void placeRandomObstacles(int count) {
    int placed = 0;
    while (placed < count) {
      int x = SAFE_ZONE_SIZE + RandomSource.get().nextInt(WIDTH - 2 * SAFE_ZONE_SIZE);
      int y = SAFE_ZONE_SIZE + RandomSource.get().nextInt(HEIGHT - 2 * SAFE_ZONE_SIZE);
      if (!obstacles[y][x]) {
        obstacles[y][x] = true;
        placed++;
      }
    }
  }

  boolean inBounds(int x, int y) {
    return x >= 0 && y >= 0 && x < WIDTH && y < HEIGHT;
  }

  boolean obstacleAt(int x, int y) {
    return obstacles[y][x];
  }

  Team safeOwner(int x, int y) {
    if (x < SAFE_ZONE_SIZE && y < SAFE_ZONE_SIZE) return Team.DOGE;
    if (x >= WIDTH - SAFE_ZONE_SIZE && y < SAFE_ZONE_SIZE) return Team.SHIBA;
    if (x < SAFE_ZONE_SIZE && y >= HEIGHT - SAFE_ZONE_SIZE) return Team.PEPE;
    if (x >= WIDTH - SAFE_ZONE_SIZE && y >= HEIGHT - SAFE_ZONE_SIZE) return Team.BONK;
    return null;
  }

  boolean isSafe(Team team, int x, int y) {
    return safeOwner(x, y) == team;
  }

  boolean mayEnter(Team team, int x, int y) {
    Team owner = safeOwner(x, y);
    return owner == null || owner == team;
  }

  /** Returns 3, 5 or 8 neighboring tiles according to the position. */
  List<Position> neighbors(int x, int y) {
    List<Position> result = new ArrayList<>();
    for (int dy = -1; dy <= 1; dy++)
      for (int dx = -1; dx <= 1; dx++) {
        if ((dx != 0 || dy != 0) && inBounds(x + dx, y + dy)) {
          result.add(new Position(x + dx, y + dy));
        }
      }
    return result;
  }

  Position directionHome(Team team, int x, int y) {
    int homeX = team == Team.DOGE || team == Team.PEPE ? 0 : WIDTH - 1;
    int homeY = team == Team.DOGE || team == Team.SHIBA ? 0 : HEIGHT - 1;
    return new Position(Integer.compare(homeX, x), Integer.compare(homeY, y));
  }
}
