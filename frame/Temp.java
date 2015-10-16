package frame;

public class Temp {
    private static int count = 30;

    private int num;

    public Temp() {
        num = count++;
    }
    public Temp(int t){
        num = t ;
    }

    public String toString() {
        return "t" + num;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(num);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Temp)
            return ((Temp) obj).num == num;

        return false;
    }

    public boolean spillTemp = false;
    public int spillCost;
}
