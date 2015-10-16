package visitor;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import minijava.analysis.DepthFirstAdapter;
import minijava.node.Node;
import minijava.node.PType;
import minijava.node.Token;
import symbol.*;

abstract class BaseAnalysis extends DepthFirstAdapter
{
    static class NodePosition implements Comparable<NodePosition> {
        public int line;
        public int pos;

        public NodePosition(Token token) {
            this.line = token.getLine();
            this.pos = token.getPos();
        }

        @Override
        public int compareTo(NodePosition other) {
            if(line < other.line || (line == other.line && pos < other.pos))
                return -1;
            if(line > other.line || (line == other.line && pos > other.pos))
                return 1;

            return 0;
        }
    }

    protected SymbolTable symbolTable;
    protected ClassS currClass;
    protected Method currMethod;

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

    public ClassS getCurrClass() {
        return currClass;
    }

    public void setCurrClass(ClassS currClass) {
        this.currClass = currClass;
    }

    public Method getCurrMethod() {
        return currMethod;
    }

    public void setCurrMethod(Method currMethod) {
        this.currMethod = currMethod;
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
        child.setCurrClass(getCurrClass());
        child.setCurrMethod(getCurrMethod());
    }

    private List<Token> getNodeTokens(Node node) {
        java.lang.Class<?> nodeClass = node.getClass();
        java.lang.reflect.Method[] allMethods = nodeClass.getDeclaredMethods();

        List<Token> tokens = new ArrayList<Token>();

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
                    tokens.addAll(getNodeTokens((Node)method.invoke(node)));
                }
            } catch (IllegalArgumentException|IllegalAccessException|InvocationTargetException e) {
                // pass
            }
        }

        return tokens;
    }

    private NodePosition getNodePosition(Node node) {
        List<Token> tokens = getNodeTokens(node);
        List<NodePosition> positions = new ArrayList<>();

        for(Token token : tokens) {
            positions.add(new NodePosition(token));
        }

        if(positions.size() > 0) {
            Collections.sort(positions);
            return positions.get(0);
        }

        return null;
    }

    void reportError(Node node, String format, Object... args) {
        for(int i = 0; i < args.length; ++i) {
            Object arg = args[i];
            if(arg instanceof Node)
                args[i] = Symbol.cleanId(arg.toString());
        }

        String message = String.format(format, args);
        NodePosition pos = getNodePosition(node);

        input.printMessageForLine(outStream, pos.line, pos.pos, "error: " + message);

        failed = true;
    }

    protected void startMethod(Method method, boolean isStatic) {
        currMethod = method;

        PType thisType = isStatic ? Symbol.identifierType("void") : currClass.getType();
        currMethod.addVar("this", thisType);
    }

    protected void endMethod(Method method) {
        if(currMethod != method)
            throw new RuntimeException("Current method changed unexpectedly");

        currMethod.removeVar("this");
        currMethod = null;
    }

    protected void applyAll(List<? extends Node> nodes) {
        for(Node node : nodes)
            node.apply(this);
    }
}
