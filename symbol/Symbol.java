package symbol;

import minijava.node.AIdentifier;
import minijava.node.AIdentifierType;
import minijava.node.PType;
import minijava.node.TId;

public abstract class Symbol {
    private String id;
    private PType type;
    private SymbolTable symbolTable;
    private Symbol parent;

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
        this.parent = null;
    }

    public Symbol(String id, PType type, Symbol parent) {
        this.id = id;
        this.symbolTable = parent.getSymbolTable();
        this.type = type;
        this.parent = parent;
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
    
    public Symbol getParent() {
        return parent;
    }
}
