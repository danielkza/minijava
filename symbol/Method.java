package symbol;

import minijava.node.PType;

import java.util.*;

public class Method extends Symbol {
    private List<Variable> params;
    private Map<String, Variable> vars;
    private ClassS ownerClass;

    public Method(String id, PType type, ClassS ownerClass) {
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
