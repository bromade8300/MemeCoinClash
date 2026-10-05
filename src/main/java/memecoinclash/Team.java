package memecoinclash;

import java.awt.Color;

enum Team {
  DOGE("Doge", "D", new Color(220, 166, 42), 0),
  SHIBA("Shiba", "S", new Color(226, 94, 72), 0),
  PEPE("Pepe", "P", new Color(64, 165, 96), 1),
  BONK("Bonk", "B", new Color(95, 116, 220), 1);
  final String name, symbol;
  final Color color;
  final int alliance;

  Team(String name, String symbol, Color color, int alliance) {
    this.name = name;
    this.symbol = symbol;
    this.color = color;
    this.alliance = alliance;
  }
}
