package antlr;

import java.util.List;

import antlr.Scope;
import antlr.Instance;
import antlr.DelphiVisitorImpl;


public class Procedure {
    private final List<String> parameters;
    private final delphiParser.BlockContext block;
    private final String functionName;

    public Procedure(List<String> parameters, delphiParser.BlockContext block) {
        this(parameters, block, null);  // default functionName is null
    }

    // Constructor for functions
    public Procedure(List<String> parameters, delphiParser.BlockContext block, String functionName) {
        this.parameters = parameters;
        this.block = block;
        this.functionName = functionName;
    }

    // public Procedure(List<String> parameters, delphiParser.BlockContext block) {
    //     this.parameters = parameters;
    //     this.block = block;
    // }

    // public Object call(List<Object> arguments, Scope callerScope, DelphiVisitorImpl visitor) {
    //     Scope localScope = new Scope(callerScope);
    
    //     for (int i = 0; i < parameters.size(); i++) {
    //         localScope.define(parameters.get(i), arguments.get(i));
    //     }
    
    //     // Correct way to fetch self for method body
    //     Object maybeSelf = localScope.resolve("self");
    //     Instance oldSelf = visitor.getCurrentSelf();
    
    //     if (maybeSelf instanceof Instance) {
    //         visitor.setCurrentSelf((Instance) maybeSelf);
    //     }
    
    //     visitor.setCurrentScope(localScope);
    //     Object result = block.accept(visitor);
    //     visitor.setCurrentScope(callerScope);
    //     visitor.setCurrentSelf(oldSelf);
    
    //     return result;
    // }

    public List<String> getParameters() {
        return parameters;
    }

    public delphiParser.BlockContext getBlock() {
        return block;
    }

    public String getFunctionName() {
        return functionName; // ✅ Added getter
    }

    // public Object call(List<Object> args, Scope callerScope, DelphiVisitorImpl visitor) {
    //     Scope procScope = new Scope(callerScope);
    
    //     for (int i = 0; i < parameters.size(); i++) {
    //         procScope.define(parameters.get(i), args.get(i));
    //     }
    
    //     // Define function name as a return variable
    //     String returnVarName = this.getFunctionName(); // you'll need to track this
    //     if (returnVarName != null) {
    //         procScope.define(returnVarName, null); // default
    //     }
    
    //     visitor.pushScope();
    //     visitor.setCurrentScope(procScope);
    //     visitor.visit(block);
    //     visitor.setCurrentScope(procScope.getParent());
    //     visitor.popScope();
    
    //     return returnVarName != null ? procScope.resolve(returnVarName) : null;
    // }
    
    public Object call(List<Object> args, Scope outerScope, DelphiVisitorImpl visitor) {
        Scope functionScope = new Scope(outerScope);
    
        // Bind parameters
        for (int i = 0; i < parameters.size(); i++) {
            functionScope.define(parameters.get(i), args.get(i));
        }
    
        // Define a variable for the function name to hold return value
        if (functionName != null && !functionName.isEmpty()) {
            functionScope.define(functionName, null);
        }
    
        // Save old scope
        Scope previousScope = visitor.getCurrentScope();
        visitor.setCurrentScope(functionScope);
    
        // Visit the block (function body)
        visitor.visit(block);
    
        // Restore old scope
        visitor.setCurrentScope(previousScope);
    
        // Return the value stored in the function name variable
        if (functionName != null && !functionName.isEmpty()) {
            return functionScope.resolve(functionName);
        } else {
            return null;
        }
    }
    
}
