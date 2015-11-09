package tree.node;

import frame.Temp;
import frame.Label;
import tree.CodeVisitor;
import tree.Exp;
import tree.IntVisitor;
import tree.ResultVisitor;

import java.util.LinkedList;

public class NAME extends Exp {
    public Label label;

    public NAME(Label l) {
        label = l;
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
