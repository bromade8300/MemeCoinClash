package memecoinclash;

final class ShibaAgent extends HypeAgent {
  static int count;

  ShibaAgent(int x, int y) {
    super(Team.SHIBA, x, y);
    count++;
  }

  @Override
  void move(Simulation g) {
    int sx = RandomSource.get().nextBoolean() ? 1 : -1;
    int sy = RandomSource.get().nextBoolean() ? 1 : -1;
    travel(g, sx, sy, 1 + RandomSource.get().nextInt(2));
  }
}
