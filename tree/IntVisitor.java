package tree;

import tree.node.*;

public interface IntVisitor {
    void visit(SEQ n, int d);

    void visit(LABEL n, int d);

    void visit(JUMP n, int d);
    
    void visit(CJUMP n, int d);

    void visit(MOVE n, int d);

    void visit(EXPR n, int d);

    void visit(BINOP n, int d);

    void visit(MEM n, int d);

    void visit(TEMP n, int d);

    void visit(ESEQ n, int d);

    void visit(NAME n, int d);

    void visit(CONST n, int d);

    void visit(CALL n, int d);
}
