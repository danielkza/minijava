package frame;

import java.util.List;

import symbol.Symbol;

public abstract class Frame {
    public Label label;
    public List<Access> formals;
    public abstract Frame newFrame(Symbol name, List<Boolean> formals);
    public abstract Frame newFrame(String name, List<Boolean> formals);
    public abstract Access allocLocal(boolean escape);
}
