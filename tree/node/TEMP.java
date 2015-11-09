package tree.node;

import frame.Temp;
import tree.CodeVisitor;
import tree.Exp;
import tree.IntVisitor;
import tree.ResultVisitor;

import java.util.LinkedList;

public class TEMP extends Exp {
    public Temp temp;

    public TEMP(Temp t) {
        temp = t;
    }

    public LinkedList<Exp> kids() {
        return new LinkedList<Exp>();
    }

    public Exp build(LinkedList<Exp> kids) {
        return this;
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
