package catchMind;

import java.awt.*;
import java.io.Serializable;

public class Dot implements Serializable {
  static int wh = 2;
  int x, y;
  Color color;

  public Dot(int x, int y, Color color) {
    this.x = x;
    this.y = y;
    this.color = color;
  }
}
