package memecoinclash;

final class DogeAgent extends HypeAgent {
  static int count;

  DogeAgent(int x, int y) {
    super(Team.DOGE, x, y);
    count++;
  }

  @Override
  void move(Simulation g) {
    int[] d = g.randomDirection();
    travel(g, d[0], d[1], 1 + RandomSource.get().nextInt(3));
  }
}
