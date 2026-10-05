package memecoinclash;

final class DogeMaster extends Master {
  private static final DogeMaster INSTANCE = new DogeMaster();

  private DogeMaster() {
    super(Team.DOGE);
  }

  static DogeMaster get() {
    return INSTANCE;
  }
}
