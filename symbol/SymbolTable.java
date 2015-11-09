package symbol;

import minijava.node.*;

import java.util.*;

public class SymbolTable {
    private Map<String, ClassS> classes;

    public SymbolTable() {
        classes = new HashMap<>();
    }

    public boolean addClass(String id, String parentName) {
        id = Symbol.cleanId(id);
        if (classes.containsKey(id))
            return false;
        
        if (parentName != null)
            parentName = Symbol.cleanId(parentName);

        ClassS cls = new ClassS(id, parentName, this);
        classes.put(id, cls);
        return true;
    }

    public ClassS getClass(String id) {
        return classes.get(Symbol.cleanId(id));
    }
    
    public Map<String, ClassS> getClasses() {
        return Collections.unmodifiableMap(classes);
    }
    
    public boolean containsClass(String id) {
        return classes.containsKey(Symbol.cleanId(id));
    }

    public boolean compareTypes(PType t1, PType t2) {
        if (t1 == null || t2 == null) return false;

        if (t1 instanceof AIdentifierType && t2 instanceof AIdentifierType) {
            AIdentifierType i1 = (AIdentifierType) t1;
            AIdentifierType i2 = (AIdentifierType) t2;

            ClassS c1 = getClass(i1.toString());
            ClassS c2 = getClass(i2.toString());

            if (c1 == null || c2 == null)
                return false;

            return c1.isCompatibleWith(c2);
        }

        return t1.getClass().equals(t2.getClass());
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        for(ClassS cls : classes.values()) {
            result.append(cls);
            result.append('\n');
        }

        return result.toString();
    }
}


