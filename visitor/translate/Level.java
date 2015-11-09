package visitor.translate;

import symbol.Symbol;
import frame.Label;

import java.util.List;
import java.util.LinkedList;

public class Level {
    public Level parent;
    frame.Frame frame;    // not public!
    public List<Access> formals = new LinkedList<Access>();

    public Level(Level p, Symbol name, List<Boolean> escapes) {
        parent = p;
        frame = parent.frame.newFrame(name, escapes);
        for (frame.Access frameAccess : frame.formals)
            formals.add(new Access(this, frameAccess));
    }

    Level(frame.Frame f) {
        frame = f;
    }

    public Label name() {
        return frame.label;
    }

    public Access allocLocal(boolean escape) {
        return new Access(this, frame.allocLocal(escape));
    }
}

