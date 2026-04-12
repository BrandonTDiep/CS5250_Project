import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    public enum Kind { STATIC, FIELD, ARG, VAR, NONE }
    
    private class Symbol {
        String type;
        Kind kind;
        int index;
        Symbol(String type, Kind kind, int index) {
            this.type = type; this.kind = kind; this.index = index;
        }
    }
    
    private Map<String, Symbol> classScope;
    private Map<String, Symbol> subroutineScope;
    private Map<Kind, Integer> indices;

    public SymbolTable() {
        classScope = new HashMap<>();
        subroutineScope = new HashMap<>();
        indices = new HashMap<>();
        indices.put(Kind.STATIC, 0);
        indices.put(Kind.FIELD, 0);
        indices.put(Kind.ARG, 0);
        indices.put(Kind.VAR, 0);
    }

    public void startSubroutine() {
        subroutineScope.clear();
        indices.put(Kind.ARG, 0);
        indices.put(Kind.VAR, 0);
    }

    public void define(String name, String type, Kind kind) {
        if (kind == Kind.STATIC || kind == Kind.FIELD) {
            classScope.put(name, new Symbol(type, kind, indices.get(kind)));
        } else if (kind == Kind.ARG || kind == Kind.VAR) {
            subroutineScope.put(name, new Symbol(type, kind, indices.get(kind)));
        }
        indices.put(kind, indices.get(kind) + 1);
    }

    public int varCount(Kind kind) {
        return indices.get(kind);
    }

    public Kind kindOf(String name) {
        if (subroutineScope.containsKey(name)) return subroutineScope.get(name).kind;
        if (classScope.containsKey(name)) return classScope.get(name).kind;
        return Kind.NONE;
    }

    public String typeOf(String name) {
        if (subroutineScope.containsKey(name)) return subroutineScope.get(name).type;
        if (classScope.containsKey(name)) return classScope.get(name).type;
        return null;
    }

    public int indexOf(String name) {
        if (subroutineScope.containsKey(name)) return subroutineScope.get(name).index;
        if (classScope.containsKey(name)) return classScope.get(name).index;
        return -1;
    }
}