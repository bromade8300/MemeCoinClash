package memecoinclash;

final class PepeMaster extends Master {
  private static final PepeMaster INSTANCE = new PepeMaster();

  private PepeMaster() {
    super(Team.PEPE);
  }

  static PepeMaster get() {
    return INSTANCE;
  }
}
