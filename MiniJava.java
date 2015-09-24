import java.io.*;

import minijava.parser.*;
import minijava.lexer.*;
import minijava.node.*;

import visitor.*;

public class MiniJava {
    public static void main(String[] args) {
        System.out.println("MiniJava v0.4");

        try {
            Input input;
            if(args.length > 0)
                input = new Input(args[0]);
            else
                input = new Input("stdin", System.in);

            // Create a lexer instance.
            Lexer lexer = new Lexer(new PushbackReader(input.getReader(), 1024));
            Parser parser = new Parser(lexer);
            Start ast = parser.parse();

            System.out.println("Construindo a tabela de simbolos...");

            //Build the symbol table
            BuildSymbolTableAnalysis symbolTableAnalysis = new BuildSymbolTableAnalysis();
            symbolTableAnalysis.setInput(input);

            ast.apply(symbolTableAnalysis);

            System.out.println("Analisando tipos...");

            TypeCheckAnalysis typeCheckAnalysis = new TypeCheckAnalysis(symbolTableAnalysis);

            ast.apply(typeCheckAnalysis);

            if(typeCheckAnalysis.hasFailed())
                System.exit(1);

            System.out.println("Type check OK.");
        } catch(LexerException|ParserException|IOException e) {
            System.out.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
