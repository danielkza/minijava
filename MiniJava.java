import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import frame.Frame;
import frame.mips.MipsFrame;
import minijava.parser.*;
import minijava.lexer.*;
import minijava.node.*;

import symbol.SymbolTable;
import visitor.*;
import visitor.translate.Frag;
import visitor.translate.ProcFrag;
import visitor.translate.Translate;

public class MiniJava {
    public static void main(String[] args) {
        System.out.println("MiniJava v0.6");

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
            
            PositionAnalysis positionAnalysis = new PositionAnalysis();
            ast.apply(positionAnalysis);
            input.setStartTokens(positionAnalysis.getStartTokens());

            System.out.println("Construindo a tabela de simbolos...");

            BuildSymbolTableAnalysis symbolTableAnalysis = new BuildSymbolTableAnalysis();
            symbolTableAnalysis.setInput(input);
            ast.apply(symbolTableAnalysis);

            System.out.println("Analisando tipos...");

            TypeCheckAnalysis typeCheckAnalysis = new TypeCheckAnalysis(symbolTableAnalysis);
            ast.apply(typeCheckAnalysis);

            if(typeCheckAnalysis.hasFailed())
                System.exit(1);
            
            SymbolTable symbolTable = symbolTableAnalysis.getSymbolTable();
            
            System.out.println("Traduzindo...");
            
            MipsFrame globalFrame =
                new MipsFrame("program", Collections.<Boolean>emptyList());
            Translate translate = new Translate(globalFrame, symbolTable,
                                                typeCheckAnalysis.getTypes());
            translate.setInput(input);
            ast.apply(translate);
            
            PrintWriter writer = new PrintWriter(System.out);
            tree.Print printer = new tree.Print(writer);//, globalFrame);
            
            List<Frag> frags = translate.getResults();
            for(Frag frag: frags) {
                 if(frag instanceof ProcFrag) {
                     ProcFrag procFrag = (ProcFrag)frag;
                     System.out.println(procFrag.frame.label.name);
                     printer.apply(procFrag.body, 0);
                     writer.println();
                     writer.flush();
                 }
            }

            if(true) {
                InterpreterVisitor interpreter = new InterpreterVisitor(frags.iterator());
                interpreter.start();
            }
        } catch(LexerException|ParserException|IOException|CompilationException e) {
            System.out.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
