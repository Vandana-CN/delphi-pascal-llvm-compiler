package antlr;

import java.util.HashMap;
import java.util.Map;

public class Scope {
    private Map<String, Object> symbols = new HashMap<>();
    private Scope parent;

    public Scope(Scope parent) {
        this.parent = parent;
    }

    public Scope getParent() {
        return parent;
    }

    // Always define names in lower-case.
    public void define(String name, Object value) {
        symbols.put(name.toLowerCase(), value);
    }

    public Object resolve(String name) {
        name = name.toLowerCase();
        if (symbols.containsKey(name.toLowerCase())) {
            return symbols.get(name.toLowerCase());
        } else if (parent != null) {
            return parent.resolve(name.toLowerCase());
        } else {
            throw new RuntimeException("Undefined symbol: " + name);
        }
    }

    public void assign(String name, Object value) {
        name = name.toLowerCase();
        if (symbols.containsKey(name)) {
            symbols.put(name, value);
        } else if (parent != null) {
            parent.assign(name, value);
        } else {
            throw new RuntimeException("Undefined symbol: " + name);
        }
    }
    
    public boolean contains(String name) {
        return symbols.containsKey(name.toLowerCase());
    }
    
    public Object get(String name) {
        return symbols.get(name.toLowerCase());
    }
}
