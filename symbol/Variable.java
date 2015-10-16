package symbol;

import minijava.node.PType;

public class Variable extends Symbol {
    public Variable(String id, PType type, Symbol parent) {
        super(cleanId(id), type, parent);
    }

    @Override
    public String toString() {
        return String.format("%s %s", getType().toString(), getId());
    }
}
