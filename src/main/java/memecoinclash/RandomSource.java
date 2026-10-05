package memecoinclash;

import java.util.Collections;
import java.util.List;
import java.util.Random;

final class RandomSource {
  private static final RandomSource INSTANCE = new RandomSource();
  private final Random random = new Random();

  private RandomSource() {}

  static RandomSource get() {
    return INSTANCE;
  }

  int nextInt(int bound) {
    return random.nextInt(bound);
  }

  boolean nextBoolean() {
    return random.nextBoolean();
  }

  void setSeed(long seed) {
    random.setSeed(seed);
  }

  <T> void shuffle(List<T> items) {
    Collections.shuffle(items, random);
  }
}
