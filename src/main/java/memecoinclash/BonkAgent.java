package memecoinclash;

final class BonkAgent extends ViralAgent {
  static int count;

  BonkAgent(int x, int y) {
    super(Team.BONK, x, y);
    count++;
  }

  @Override
  void move(Simulation g) {
    int[] d = g.randomDirection();
    travel(g, d[0], d[1], 1 + RandomSource.get().nextInt(2));
  }
}
