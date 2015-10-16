package frame;

import symbol.Symbol;

/**
 * A Label represents an address in assembly language.
 */

public class Label  {
   public String name;
   private static int count = 1;

   public String toString() {return name;}

   public Label(String n) {
  name=n;
   }

   public Label() {
  this("L" + count++);
   }

   public Label(Symbol s) {
       this(s.getId());
   }
}
