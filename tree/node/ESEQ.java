package tree.node;

import frame.Temp;
import tree.*;

import java.util.LinkedList;

public class ESEQ extends Exp {
    public Stm stm;
    public Exp exp;

    public ESEQ(Stm s, Exp e) {
        stm = s;
        exp = e;
    }

    public LinkedList<Exp> kids() {
        throw new Error("kids() not applicable to ESEQ");
    }

    public Exp build(LinkedList<Exp> kids) {
        throw new Error("build() not applicable to ESEQ");
    }

    public void accept(IntVisitor v, int d) {
        v.visit(this, d);
    }

    public Temp accept(CodeVisitor v) {
        return v.visit(this);
    }

    public <R> R accept(ResultVisitor<R> v) {
        return v.visit(this);
    }
}
