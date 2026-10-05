package memecoinclash;

abstract class Master extends Agent {
  Master(Team team) {
    super(team, 0, 0);
  }

  void clear() {
    messages.clear();
    energy = 24;
    exhausted = false;
  }

  @Override
  final void move(Simulation game) {}
}
