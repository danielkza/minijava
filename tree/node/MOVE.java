package tree.node;

import tree.*;

import java.util.LinkedList;

public class MOVE extends Stm {
    public Exp dst, src;

    public MOVE(Exp d, Exp s) {
        dst = d;
        src = s;
    }

    public LinkedList<Exp> kids() {
        LinkedList<Exp> kids = new LinkedList<Exp>();
        if (dst instanceof MEM) {
            kids.addFirst(((MEM) dst).exp);
            kids.addLast(src);
        } else
            kids.addFirst(src);
        return kids;
    }

    public Stm build(LinkedList<Exp> kids) {
        if (dst instanceof MEM)
            return new MOVE(new MEM(kids.getFirst()), kids.getLast());
        else return new MOVE(dst, kids.getFirst());
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
