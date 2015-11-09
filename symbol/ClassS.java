package symbol;

import minijava.node.PType;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ClassS extends Symbol {
    private LinkedHashMap<String, Method> methods = new LinkedHashMap<>();
    private LinkedHashMap<String, Variable> globals = new LinkedHashMap<>();
    
    private String baseClassName;
    private ClassS baseClass;
    
    private Variable thisVar;
    
    public ClassS(String id, String baseClassName, SymbolTable symbolTable) {
        super(cleanId(id), identifierType(id), symbolTable);
        this.baseClassName = baseClassName;
        
        thisVar = new Variable("this", getType(), this);
    }
    
    public Variable getThisVar() {
        return thisVar;
    }
    

    public boolean addMethod(String id, PType type) {
        id = cleanId(id);
        if (methods.containsKey(id))
            return false;

        methods.put(id, new Method(id, type, this));
        return true;
    }

    public Method getMethod(String id) {
        return methods.get(cleanId(id));
    }

    public boolean containsMethod(String id) {
        return methods.containsKey(cleanId(id));
    }

    public Map<String, Method> getMethods() {
        return Collections.unmodifiableMap(methods);
    }

    public Method getMethodInHierarchy(String id) {
        id = cleanId(id);

        ClassS curClass = this;
        while(curClass != null) {
            Method method = curClass.getMethod(id);
            if(method != null)
                return method;

            curClass = curClass.getBaseClass();
        }

        return null;
    }

    public boolean addVar(String id, PType type) {
        id = cleanId(id);

        if (globals.containsKey(id))
            return false;

        globals.put(id, new Variable(id, type, this));
        return true;
    }

    public Variable getVar(String id) {
        return globals.get(cleanId(id));
    }

    public Map<String, Variable> getVars() {
        return Collections.unmodifiableMap(globals);
    }
    
    public boolean containsVar(String id) {
        return globals.containsKey(cleanId(id));
    }
    
    public String getBaseClassName() {
        if(baseClass == null) {
            return baseClassName;
        } else {
            return baseClass.getId();
        }
    }

    public ClassS getBaseClass() {
        if(baseClassName != null && baseClass == null)
            baseClass = findParentClass(baseClassName, getSymbolTable());
        
        return baseClass;
    }

    private static ClassS findParentClass(String className,
                                          SymbolTable symbolTable) {
        if(className == null)
            return null;

        ClassS parentClass = symbolTable.getClass(className);
        if(parentClass == null) {
            String message = String.format(
                "Attempted to get parent class '%s', but it was not defined on the symbol table",
                className);

            throw new RuntimeException(message);
        }

        return parentClass;
    }

    public Variable getIdentifier(String id) {
        id = cleanId(id);

        ClassS curClass = this;
        do {
          Variable var = curClass.getVar(id);
          if(var != null)
              return var;

          curClass = curClass.getBaseClass();
        } while(curClass != null);

        return null;
    }

    public boolean isCompatibleWith(ClassS other) {
        ClassS curClass = this;
        do {
            if(curClass.equals(other))
                return true;

            curClass = curClass.getBaseClass();
        } while(curClass != null) ;

        return false;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("class "); result.append(getId()); result.append(" {\n");
        for(Method method : methods.values()) {
            result.append(method); result.append('\n');
        }
        result.append('}');

        return result.toString();
    }
}
