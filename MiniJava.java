import java.io.*;

import minijava.parser.*;
import minijava.lexer.*;
import minijava.node.*;
import minijava.analysis.*;

import visitor.*;

public class MiniJava {
  public static void main(String[] arguments) {
    try {
      Lexer lexer = new Lexer(new PushbackReader
        (new InputStreamReader(System.in), 1024));
      Parser parser = new Parser(lexer);
      Start ast = parser.parse();

      Analysis printer = new PrettyPrinter();
      ast.apply(printer);
    } catch(Exception e) {
      System.out.println(e.getMessage());
    }
  }
}
