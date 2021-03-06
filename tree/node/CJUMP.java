package tree.node;

import frame.Label;
import tree.*;

import java.util.LinkedList;

public class CJUMP extends Stm {
    public int relop;
    public Exp left, right;
    public Label iftrue, iffalse;

    public CJUMP(int rel, Exp l, Exp r, Label t, Label f) {
        relop = rel;
        left = l;
        right = r;
        iftrue = t;
        iffalse = f;
    }

    public final static int EQ = 0, NE = 1, LT = 2, GT = 3, LE = 4, GE = 5, ULT = 6, ULE = 7,
            UGT = 8, UGE = 9;

    public LinkedList<Exp> kids() {
        LinkedList<Exp> kids = new LinkedList<Exp>();
        kids.addFirst(left);
        kids.addLast(right);
        return kids;
    }

    public Stm build(LinkedList<Exp> kids) {
        return new
                CJUMP(relop, kids.getFirst(), kids.getLast(), iftrue, iffalse);
    }

    public static int notRel(int relop) {
        switch (relop) {
            case EQ:
                return NE;
            case NE:
                return EQ;
            case LT:
                return GE;
            case GE:
                return LT;
            case GT:
                return LE;
            case LE:
                return GT;
            case ULT:
                return UGE;
            case UGE:
                return ULT;
            case UGT:
                return ULE;
            case ULE:
                return UGT;
            default:
                throw new Error("bad relop in CJUMP.notRel");
        }
    }

    public void accept(IntVisitor v, int d) {
        v.visit(this, d);
    }

    public void accept(CodeVisitor v) {
        v.visit(this);
    }

    public <R> R accept(ResultVisitor<R> v) {
        return v.visit(this);
    }
}
