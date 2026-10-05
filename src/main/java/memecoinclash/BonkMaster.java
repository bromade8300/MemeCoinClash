package memecoinclash;

final class BonkMaster extends Master {
  private static final BonkMaster INSTANCE = new BonkMaster();

  private BonkMaster() {
    super(Team.BONK);
  }

  static BonkMaster get() {
    return INSTANCE;
  }
}
