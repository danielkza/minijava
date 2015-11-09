package frame;

import tree.Exp;
import tree.node.*;

public class InReg extends Access {
    public Temp temp;
    
    public InReg(Temp t) {
        temp = t;
    }
    
    public String toString() {
        return temp.toString();
    }
    
    public Exp exp(Exp fp) {
        return new TEMP(temp);
    }
}
