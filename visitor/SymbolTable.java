package visitor;

import minijava.node.*;

import java.util.*;

public class SymbolTable {
    private Map<String, Class> classes;

    public SymbolTable() {
        classes = new HashMap<>();
    }

    public boolean addClass(String id, String parent) {
        id = Symbol.cleanId(id);

        if (parent != null)
            parent = Symbol.cleanId(parent);

        if (classes.containsKey(id))
            return false;

        Class cls = new Class(id, parent, this);
        classes.put(id, cls);
        return true;
    }

    public Class getClass(String id) {
        return classes.get(Symbol.cleanId(id));
    }

    public boolean containsClass(String id) {
        return classes.containsKey(Symbol.cleanId(id));
    }

    public boolean compareTypes(PType t1, PType t2) {
        if (t1 == null || t2 == null) return false;

        if (t1 instanceof AIdentifierType && t2 instanceof AIdentifierType) {
            AIdentifierType i1 = (AIdentifierType) t1;
            AIdentifierType i2 = (AIdentifierType) t2;

            Class c1 = getClass(i1.toString());
            Class c2 = getClass(i2.toString());

            if (c1 == null || c2 == null)
                return false;

            return c1.isCompatibleWith(c2);
        }

        return t1.getClass().equals(t2.getClass());
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        for(Class cls : classes.values()) {
            result.append(cls);
            result.append('\n');
        }

        return result.toString();
    }
}

abstract class Symbol  {
    private String id;
    private PType type;
    private SymbolTable symbolTable;

    public static String cleanId(String id) {
        return id.replaceAll("\\s+", "");
    }

    public static AIdentifierType identifierType(String id) {
        return new AIdentifierType(new AIdentifier(new TId(cleanId(id))));
    }

    public Symbol(String id, PType type, SymbolTable symbolTable) {
        this.id = id;
        this.symbolTable = symbolTable;
        this.type = type;
    }

    public Symbol(String id, PType type, Symbol parent) {
        this(id, type, parent.getSymbolTable());
    }

    public String getId() {
        return id;
    }

    public PType getType() {
        return type;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
}

class Class extends Symbol {
    private Map<String, Method> methods;
    private Map<String, Variable> globals;

    private String parent;
    private Class parentClass;

    public Class(String id, String parent, SymbolTable symbolTable) {
        super(cleanId(id), identifierType(id), symbolTable);

        if (parent != null)
            parent = cleanId(parent);

        this.parent = parent;
        methods = new HashMap<>();
        globals = new HashMap<>();
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

        Class curClass = this;
        while(curClass != null) {
            Method method = curClass.getMethod(id);
            if(method != null)
                return method;

            curClass = curClass.getParentClass();
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

    public boolean containsVar(String id) {
        return globals.containsKey(cleanId(id));
    }

    public String getParent() {
        return parent;
    }

    public Class getParentClass() {
        if(parent == null)
            return null;

        if(parentClass == null) {
            parentClass = getSymbolTable().getClass(parent);
            if(parentClass == null) {
                String message = String.format(
                    "Attempted to get parent class '%s', but it was not defined on the symbol table",
                    parent);

                throw new RuntimeException(message);
            }
        }

        return parentClass;
    }

    public Variable getIdentifier(String id) {
        id = cleanId(id);

        Class curClass = this;
        while(curClass != null) {
          Variable var = curClass.getVar(id);
          if(var != null)
              return var;

          curClass = curClass.getParentClass();
        }

        return null;
    }

    public boolean isCompatibleWith(Class other) {
        Class curClass = this;
        while(curClass != null) {
            if(curClass.equals(other))
                return true;

            curClass = curClass.getParentClass();
        }

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

class Variable extends Symbol {
    public Variable(String id, PType type, Symbol parent) {
        super(cleanId(id), type, parent);
    }

    @Override
    public String toString() {
        return String.format("%s %s", getType().toString(), getId());
    }
}

class Method extends Symbol {
    private List<Variable> params;
    private Map<String, Variable> vars;
    private Class ownerClass;

    public Method(String id, PType type, Class ownerClass) {
        super(cleanId(id), type, ownerClass);

        this.ownerClass = ownerClass;
        vars = new HashMap<>();
        params = new ArrayList<>();
    }

    public boolean addParam(String id, PType type) {
        id = cleanId(id);

        if (containsParam(id))
            return false;

        params.add(new Variable(id, type, this));
        return true;
    }

    public Variable getParam(String id) {
        id = cleanId(id);

        for(Variable param : params)
            if(param.getId().equals(id))
                return param;

        return null;
    }

    public List<Variable> getParams() {
        return Collections.unmodifiableList(params);
    }

    public boolean containsParam(String id) {
        return getParam(id) != null;
    }

    public boolean addVar(String id, PType type) {
        id = cleanId(id);

        if (vars.containsKey(id))
            return false;

        vars.put(id, new Variable(id, type, this));
        return true;
    }

    public void removeVar(String id) {
        vars.remove(cleanId(id));

    }

    public boolean containsVar(String id) {
        return vars.containsKey(cleanId(id));
    }

    public Variable getVar(String id) {
        return vars.get(cleanId(id));
    }

    public Map<String, Variable> getVars() {
        return Collections.unmodifiableMap(vars);
    }

    public Variable getIdentifier(String id) {
        Variable identifier = getParam(id);
        if(identifier != null)
            return identifier;

        identifier = getVar(id);
        if(identifier != null)
            return identifier;

        if(ownerClass != null)
            identifier = ownerClass.getIdentifier(id);

        return identifier;
    }

    public PType getReturnType() {
        return getType();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append(getType()); result.append(' '); result.append(getId());
        result.append('(');
        for(Variable param : params) {
            result.append(param);
            result.append(", ");
        }
        result.append(") {\n");
        for(Variable var : vars.values()) {
            result.append(var);
            result.append(";\n");
        }
        result.append('}');

        return result.toString();
    }
}


