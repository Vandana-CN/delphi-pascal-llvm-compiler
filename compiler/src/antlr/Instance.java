package antlr;

import java.util.HashMap;
import java.util.Map;

public class Instance {
    private Map<String, Object> fields = new HashMap<>();
    private Map<String, Procedure> methods = new HashMap<>();
    private String className; // NEW FIELD

    public Instance(String className) { // NEW CONSTRUCTOR
        this.className = className.toLowerCase();
    }

    public String getClassName() {
        return className;
    }

    public void setField(String fieldName, Object value) {
        fields.put(fieldName.toLowerCase(), value);
    }

    public Object getField(String fieldName) {
        return fields.get(fieldName.toLowerCase());
    }

    public boolean hasField(String fieldName) {
        return fields.containsKey(fieldName.toLowerCase());
    }

    public void setMethod(String methodName, Procedure proc) {
        methods.put(methodName.toLowerCase(), proc);
    }

    public Procedure getMethod(String methodName) {
        return methods.get(methodName.toLowerCase());
    }

    @Override
    public String toString() {
        return "Instance of " + className + " with fields: " + fields.toString();
    }
}
