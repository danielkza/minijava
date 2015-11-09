package tree;

public abstract class Hospitable {
    public String tag = null;
    
    abstract void accept(IntVisitor v, int d);

    abstract <R> R accept(ResultVisitor<R> v);
}
