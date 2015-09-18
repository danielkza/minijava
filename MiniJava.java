import minijava.parser.*;
import minijava.lexer.*;
import minijava.node.*;
import java.io.*;

public class MiniJava {
  public static void main(String[] arguments) {
    try {
      Lexer lexer = new Lexer(new PushbackReader
        (new InputStreamReader(System.in), 1024));
      Parser parser = new Parser(lexer);
      Start ast = parser.parse();
      System.out.println(ast.toString());
    } catch(Exception e) {
      System.out.println(e.getMessage());
    }
  }
}
