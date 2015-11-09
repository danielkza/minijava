package frame;

import java.util.Collections;
import java.util.List;

import symbol.Symbol;
import symbol.SymbolTable;

public abstract class Frame {
    public Label label;
    public List<Access> formals;
    public abstract Frame newFrame(Symbol name, List<Boolean> formals);
    public abstract Frame newFrame(String name, List<Boolean> formals);
    public Frame newFrame(Symbol name) {
        return newFrame(name, Collections.<Boolean>emptyList());
    }
    public abstract Access allocLocal(boolean escape);

    public abstract Temp FP();
    public abstract Temp RV();
    public abstract Access FPaccess();
    public abstract Access RVaccess();

    public abstract String programEpilogue();
    
    public abstract int getOffset();
    public abstract void setOffset(int offset);
}
