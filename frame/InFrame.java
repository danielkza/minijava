package frame;

import tree.*;
import tree.node.*;

public class InFrame extends Access {
    public int offset;

    public InFrame(int offset) {
        this.offset = offset;
    }

    public String toString() {
        return Integer.toString(this.offset);
    }
    
    public Exp exp(Exp fp) {
        return new MEM(new BINOP(BINOP.PLUS, fp, new CONST(offset)));
    }
}
