package tree;

import tree.node.*;

public interface ResultVisitor<R>{
    R visit(SEQ n);

    R visit(LABEL n);

    R visit(JUMP n);

    R visit(CJUMP n);

    R visit(MOVE n);

    R visit(EXPR n);

    R visit(BINOP n);

    R visit(MEM n);

    R visit(TEMP n);

    R visit(ESEQ n);

    R visit(NAME n);

    R visit(CONST n);

    R visit(CALL n);
}

