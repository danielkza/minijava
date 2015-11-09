package visitor.translate;

public class Access {
    Level home;
    frame.Access acc;

    Access(Level h, frame.Access a) {
        home = h;
        acc = a;
    }

    public String toString() {
        return "[" + home.frame.toString() + "," + acc.toString() + "]";
    }
}
