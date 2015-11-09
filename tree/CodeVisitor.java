package tree;

import frame.Temp;
import tree.node.*;

public interface CodeVisitor {
    void visit(SEQ n);

    void visit(LABEL n);

    void visit(JUMP n);

    void visit(CJUMP n);

    void visit(MOVE n);

    void visit(EXPR n);

    Temp visit(BINOP n);

    Temp visit(MEM n);

    Temp visit(TEMP n);

    Temp visit(ESEQ n);

    Temp visit(NAME n);

    Temp visit(CONST n);

    Temp visit(CALL n);
}
