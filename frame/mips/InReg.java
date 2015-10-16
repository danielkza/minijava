package frame.mips;

import frame.Temp;
import frame.Access;

public class InReg implements Access {
  Temp temp;

  InReg(Temp t) {
    temp = t;
  }

  public String toString() {
    return temp.toString();
  }
}
