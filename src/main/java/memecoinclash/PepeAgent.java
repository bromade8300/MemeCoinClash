package memecoinclash;

final class PepeAgent extends ViralAgent {
  static int count;

  PepeAgent(int x, int y) {
    super(Team.PEPE, x, y);
    count++;
  }

  @Override
  void move(Simulation g) {
    int[] d = g.randomDirection();
    travel(g, d[0], d[1], 1);
  }
}
