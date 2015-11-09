package visitor;

import minijava.node.Node;
import minijava.node.Token;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Input {
    static class Line {
        public int start;
        public int end;

        public Line(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    private String fileName;
    private String filePath;
    private String content;
    private List<Line> linePositions = new ArrayList<>();
    private Map<Node, Token> startTokens = null;

    public Input(String filePath, InputStream inputStream) throws IOException {
        this.filePath = filePath;
        this.fileName = new File(filePath).getName();

        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[1024];
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        while (true) {
            int readLen = reader.read(buffer, 0, buffer.length);
            if (readLen == -1)
                break;

            builder.append(buffer, 0, readLen);
        }

        content = builder.toString();

        int prevIndex = 0, index = 0;
        while ((index = content.indexOf('\n', prevIndex)) != -1) {
            linePositions.add(new Line(prevIndex, index));
            prevIndex = index + 1;
        }
    }

    public Input(String fileName) throws IOException {
        this(fileName, new FileInputStream(fileName));
    }

    public Reader getReader() {
        return new StringReader(content);
    }

    public void setStartTokens(Map<Node, Token> startTokens) {
        this.startTokens = startTokens;
    }
    
    public void printMessageForNode(PrintStream outputStream, Node node,
                                    String message) {
        Token token;
        if(node instanceof Token)
            token = (Token)node;
        else
            token = startTokens.get(node);
        
        int line = token.getLine();
        int pos = token.getPos();
        
        outputStream.format("%s:%d: %s", fileName, line, message);
        outputStream.println();

        try {
            Line linePos = linePositions.get(line - 1);

            outputStream.println(content.substring(linePos.start, linePos.end));
            for (int i = 1; i < pos; ++i)
                outputStream.print(' ');

            outputStream.print('^');
        } catch (IndexOutOfBoundsException e) {
            // pass
        }

        outputStream.println();
    }
    
    public String line(int line) {
        Line linePos = linePositions.get(line - 1);
        return content.substring(linePos.start, linePos.end);
    }
    
    public String nodeLine(Node node) {
        Token token;
        if(node instanceof Token)
            token = (Token)node;
        else
            token = startTokens.get(node);
        
        return line(token.getLine());
    }
}
