package visitor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

    public Input(String fileName) throws FileNotFoundException, IOException {
        this(fileName, new FileInputStream(fileName));
    }

    public Reader getReader() {
        return new StringReader(content);
    }

    public void printMessageForLine(PrintStream outputStream, int line, int pos,
                                    String message) {
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
}
