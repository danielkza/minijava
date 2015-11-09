package tree;

import java.util.LinkedList;

abstract public class Stm extends Hospitable {
    public abstract LinkedList<Exp> kids();

    public abstract Stm build(LinkedList<Exp> kids);

    public abstract void accept(IntVisitor v, int d);

    public abstract <R> R accept(ResultVisitor<R> v);

    public abstract void accept(CodeVisitor v);
}
