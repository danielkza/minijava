package visitor;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import minijava.analysis.DepthFirstAdapter;
import minijava.node.Node;
import minijava.node.PType;
import minijava.node.Token;
import symbol.*;

public abstract class BaseAnalysis extends DepthFirstAdapter
{
    protected SymbolTable symbolTable;
    protected ClassS currentClass;
    protected Method currentMethod;

    protected PrintStream outStream;
    protected Input input;

    private boolean failed = false;

    public BaseAnalysis() {
        this.outStream = System.err;
        this.symbolTable = new SymbolTable();
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public ClassS getCurrentClass() {
        return currentClass;
    }

    public void setCurrentClass(ClassS currentClass) {
        this.currentClass = currentClass;
    }

    public Method getCurrentMethod() {
        return currentMethod;
    }

    public void setCurrentMethod(Method currentMethod) {
        this.currentMethod = currentMethod;
    }

    public void setSymbolTable(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public void setOutStream(PrintStream outStream) {
        this.outStream = outStream;
    }

    public PrintStream getOutStream() {
        return outStream;
    }

    public Input getInput() {
        return input;
    }

    public void setInput(Input input) {
        this.input = input;
    }

    public boolean hasFailed() {
        return failed;
    }

    public void prepareChild(BaseAnalysis child) {
        child.setOutStream(outStream);
        child.setInput(getInput());
        child.setSymbolTable(getSymbolTable());
        child.setCurrentClass(getCurrentClass());
        child.setCurrentMethod(getCurrentMethod());
    }

    private Set<Token> getNodeTokens(Node node) {
        Set<Token> tokens = new HashSet<>();
        getNodeTokens(node, tokens);
        
        return tokens;
    }
    
    private void getNodeTokens(Node node, Set<Token> tokens) {
        java.lang.Class<?> nodeClass = node.getClass();
        java.lang.reflect.Method[] allMethods = nodeClass.getDeclaredMethods();

        for(java.lang.reflect.Method method : allMethods) {
            try {
                if(!method.getName().startsWith("get"))
                    continue;

                if((method.getModifiers() & Modifier.PUBLIC) == 0)
                    continue;

                if(method.getParameterTypes().length > 0)
                    continue;

                if (Token.class.isAssignableFrom(method.getReturnType())) {
                    tokens.add((Token)method.invoke(node));
                } else if(Node.class.isAssignableFrom(method.getReturnType())) {
                    getNodeTokens((Node)method.invoke(node), tokens);
                } else if(List.class.isAssignableFrom(method.getReturnType())) {
                    List<?> values = List.class.cast(method.invoke(node));
                    for(Object elm : values) {
                        if(elm instanceof Token)
                            tokens.add((Token)elm);
                        else if(elm instanceof Node)
                            getNodeTokens((Node)elm, tokens);
                    }
                }
            } catch (IllegalArgumentException|IllegalAccessException|InvocationTargetException e) {
                // pass
            }
        }
    }
    
    public void reportError(Node node, String format, Object... args) {
        for(int i = 0; i < args.length; ++i) {
            Object arg = args[i];
            if(arg == null)
                args[i] = "`null`";
            else if(arg instanceof Node)
                args[i] = Symbol.cleanId(arg.toString());
        }

        String message = String.format(format, args);
        input.printMessageForNode(outStream, node, "error: " + message);

        failed = true;
    }
    
    public void fail(Node node, String format, Object... args) {
        reportError(node, format, args);
        throw new CompilationException();
    }

    protected void startMethod(Method method, boolean isStatic) {
        currentMethod = method;

        PType thisType = isStatic ? Symbol.identifierType("void") : currentClass.getType();
        currentMethod.addVar("this", thisType);
    }

    protected void endMethod(Method method) {
        if(currentMethod != method)
            throw new RuntimeException("Current method changed unexpectedly");

        currentMethod.removeVar("this");
        currentMethod = null;
    }

    protected void applyAll(List<? extends Node> nodes) {
        for(Node node : nodes)
            node.apply(this);
    }
}
