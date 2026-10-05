package memecoinclash;

final class ShibaMaster extends Master {
  private static final ShibaMaster INSTANCE = new ShibaMaster();

  private ShibaMaster() {
    super(Team.SHIBA);
  }

  static ShibaMaster get() {
    return INSTANCE;
  }
}
