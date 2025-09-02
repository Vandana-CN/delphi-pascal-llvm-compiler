package antlr;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * DelphiVisitorImpl traverses the AST generated from the extended Delphi grammar.
 * It echoes the parsed constructs (classes, variables, assignments, procedure calls, etc.)
 * to demonstrate the language features such as classes, object instantiation, and encapsulation.
 */

public class DelphiVisitorImpl extends delphiBaseVisitor<Object> {
    private final Scanner scanner;
    Map<String, delphiParser.FunctionDeclarationContext> functionDeclarations = new HashMap<>();

    private Deque<String> breakLabelStack = new ArrayDeque<>();
    private Deque<String> continueLabelStack = new ArrayDeque<>();

    public class ContinueException extends RuntimeException {}
    public class BreakException extends RuntimeException {}


     Instance currentSelf = null;

     public Instance getCurrentSelf() {
        return currentSelf;
    }
    
    public void setCurrentSelf(Instance self) {
        this.currentSelf = self;
    }

private final Map<String, List<String>> classFields = new HashMap<>();

 
    private Scope currentScope = new Scope(null); // p2


   
    
    public void pushScope() {
        // Create a new scope with the current scope as its parent.
        Scope newScope = new Scope(currentScope);
        // If the current scope has a binding for "self", copy it into the new scope.
        try {
            Object selfVal = currentScope.resolve("self");
            newScope.define("self", selfVal);
        } catch (RuntimeException e) {
            // "self" is not defined in the current scope; nothing to copy.
        }
        currentScope = newScope;
    }
    
    
    public void popScope() {
        currentScope = currentScope.getParent();
    }
     //p2
    

    public void setCurrentScope(Scope scope) {
        this.currentScope = scope;
    }
    
    public Scope getCurrentScope() {
        return this.currentScope;
    }
    
           


    public DelphiVisitorImpl() {
        // Initialize the scanner for input operations (e.g., readln)
        this.scanner = new Scanner(System.in);
    }

    // --- Program and Class Declarations ---

    /**
     * Visit the entire program.
     * @param ctx The ProgramContext.
     * @return null
     */
    @Override
    public Object visitProgram(delphiParser.ProgramContext ctx) {
        // System.out.println("Starting program parsing...");
        // return visitChildren(ctx);

        //3

        LLVMGenerator.startProgram();           // Begin LLVM IR program
        Object result = visitChildren(ctx);     // Visit rest of the program
        LLVMGenerator.endProgram();             // Close function and add return
        LLVMGenerator.writeToFile("output.ll"); // Save IR to file
        return result;
    }

    /**
     * Visit a class type declaration.
     * Prints the class name and its member declarations (variables and procedures).
     * @param ctx The ClassTypeContext.
     * @return null
     */
    @Override
    public Object visitClassType(delphiParser.ClassTypeContext ctx) {
        String className = "UnnamedClass";
        if (ctx.getParent() instanceof delphiParser.TypeDefinitionContext) {
            delphiParser.TypeDefinitionContext typeDef = (delphiParser.TypeDefinitionContext) ctx.getParent();
            className = typeDef.identifier().getText().toLowerCase(); // Normalize to lowercase
            System.out.println("Class declared: " + className);
        }
    
        List<String> fields = new ArrayList<>();
    
        if (ctx.classBody() != null) {
            for (delphiParser.ClassMemberContext member : ctx.classBody().classMember()) {
                if (member.variableDeclaration() != null) {
                    String varList = member.variableDeclaration().identifierList().getText();
                    System.out.println("  Variable: " + varList + " of type " + member.variableDeclaration().type_().getText());
    
                    for (String var : varList.split(",")) {
                        fields.add(var.trim().toLowerCase()); // Normalize to lowercase
                    }
                } else if (member.procedureDeclaration() != null &&
                           !member.procedureDeclaration().identifier().isEmpty()) {
                    if (member.procedureDeclaration().identifier().size() >= 2) {
                        System.out.println("  Procedure: " +
                            member.procedureDeclaration().identifier(0).getText() + "." +
                            member.procedureDeclaration().identifier(1).getText());
                    } else {
                        System.out.println("  Procedure: " + member.procedureDeclaration().getText());
                    }
                }
            }
        }
    
        classFields.put(className, fields); // Save fields for this class
        return visitChildren(ctx);
    }
    


    //p2 

//     @Override
// public Void visitBreakStatement(delphiParser.BreakStatementContext ctx) {
//     throw new BreakException();
// }

// @Override
// public Void visitContinueStatement(delphiParser.ContinueStatementContext ctx) {
//     throw new ContinueException();
// }


// @Override
// public Object visitFunctionDeclaration(delphiParser.FunctionDeclarationContext ctx) {
//     String name = ctx.identifier().getText().toLowerCase();
//     functionDeclarations.put(name, ctx);
//     return null;
// }

// @Override
// public Object visitFunctionDeclaration(delphiParser.FunctionDeclarationContext ctx) {
//     List<String> parameters = new ArrayList<>();
//     if (ctx.formalParameterList() != null) {
//         for (delphiParser.FormalParameterSectionContext section : ctx.formalParameterList().formalParameterSection()) {
//             if (section.parameterGroup() != null) {
//                 for (delphiParser.IdentifierContext idCtx : section.parameterGroup().identifierList().identifier()) {
//                     parameters.add(idCtx.getText().toLowerCase());
//                 }
//             }
//         }
//     }

//     String functionName = ctx.identifier().getText().toLowerCase();
//     delphiParser.BlockContext body = ctx.block();
//     Procedure functionProc = new Procedure(parameters, body);
//     currentScope.define(functionName, functionProc);

//     System.out.println("Function Declaration stored: " + functionName + " with parameters: " + parameters);
//     return null;
// }
    //p2 for
    @Override
    public Object visitFunctionDeclaration(delphiParser.FunctionDeclarationContext ctx) {
        List<String> parameters = new ArrayList<>();
        if (ctx.formalParameterList() != null) {
            for (delphiParser.FormalParameterSectionContext section : ctx.formalParameterList().formalParameterSection()) {
                if (section.parameterGroup() != null) {
                    for (delphiParser.IdentifierContext idCtx : section.parameterGroup().identifierList().identifier()) {
                        parameters.add(idCtx.getText().toLowerCase());
                    }
                }
            }
        }
    
        String funcName = ctx.identifier().getText().toLowerCase();
        delphiParser.BlockContext body = ctx.block();
    
        Procedure func = new Procedure(parameters, body, funcName);
        currentScope.define(funcName, func);
    
        System.out.println("Function Declaration stored: " + funcName + " with parameters: " + parameters);
        return null;
    }

//     @Override
// public Void visitForStatement(delphiParser.ForStatementContext ctx) {
//     // Extract the loop variable.
//     String varName = ctx.identifier().getText();
//     // Get the initial and final values from the forList rule.
//     // Assuming forList is defined as: initialValue (TO | DOWNTO) finalValue
//     Object initValObj = visit(ctx.forList().getChild(0));   // initialValue
//     String direction = ctx.forList().getChild(1).getText();   // TO or DOWNTO
//     Object finalValObj = visit(ctx.forList().getChild(2));     // finalValue

//     // Ensure that the initial and final values are integers.
//     if (!(initValObj instanceof Integer) || !(finalValObj instanceof Integer)) {
//         throw new RuntimeException("For loop initial and final values must be integers.");
//     }
//     int initVal = (Integer) initValObj;
//     int finalVal = (Integer) finalValObj;

//     // Define the loop variable in the current scope.
//     currentScope.define(varName, initVal);

//     // Iterate based on the loop direction.
//     if ("TO".equalsIgnoreCase(direction)) {
//         for (int i = initVal; i <= finalVal; i++) {
//             currentScope.assign(varName, i);
//             pushScope();  // New scope for this iteration.
//             try {
//                 visit(ctx.statement());
//             } catch (BreakException be) {
//                 popScope();
//                 break;
//             } catch (ContinueException ce) {
//                 // Continue to the next iteration.
//             } finally {
//                 popScope();
//             }
//         }
//     } else if ("DOWNTO".equalsIgnoreCase(direction)) {
//         for (int i = initVal; i >= finalVal; i--) {
//             currentScope.assign(varName, i);
//             pushScope();
//             try {
//                 visit(ctx.statement());
//             } catch (BreakException be) {
//                 popScope();
//                 break;
//             } catch (ContinueException ce) {
//                 // Continue to the next iteration.
//             } finally {
//                 popScope();
//             }
//         }
//     } else {
//         throw new RuntimeException("For loop uses unknown direction: " + direction);
//     }
//     return null;
// } --3


@Override
public Object visitForStatement(delphiParser.ForStatementContext ctx) {
    String loopVar = ctx.identifier().getText().toLowerCase();
    Object startVal = visit(ctx.forList().initialValue());
    Object endVal = visit(ctx.forList().finalValue());

    LLVMGenerator.declareVariable(loopVar);
    LLVMGenerator.store(startVal.toString(), loopVar);
    currentScope.define(loopVar, startVal);

    String condLabel = LLVMGenerator.nextLabel("cond");
    String bodyLabel = LLVMGenerator.nextLabel("body");
    String incrLabel = LLVMGenerator.nextLabel("incr");
    String endLabel = LLVMGenerator.nextLabel("end");

    // Push labels for break and continue
    breakLabelStack.push(endLabel);
    continueLabelStack.push(incrLabel);

    LLVMGenerator.br(condLabel);

    LLVMGenerator.label(condLabel);
    String loaded = LLVMGenerator.load(loopVar);
    String cond = LLVMGenerator.icmp("sle", loaded, endVal.toString());
    LLVMGenerator.brCond(cond, bodyLabel, endLabel);

    LLVMGenerator.label(bodyLabel);
    pushScope();
    try {
        visit(ctx.statement());  // may contain break/continue
    } catch (ContinueException ce) {
        LLVMGenerator.br(incrLabel);
    } catch (BreakException be) {
        LLVMGenerator.br(endLabel);
    } finally {
        popScope();
    }

    LLVMGenerator.br(incrLabel);

    LLVMGenerator.label(incrLabel);
    String reloaded = LLVMGenerator.load(loopVar);
    String incremented = LLVMGenerator.add(reloaded, "1");
    LLVMGenerator.store(incremented, loopVar);
    LLVMGenerator.br(condLabel);

    LLVMGenerator.label(endLabel);

    // Pop labels after loop
    breakLabelStack.pop();
    continueLabelStack.pop();

    return null;
}



//constructor call p2
@Override
public Object visitConstructorCall(delphiParser.ConstructorCallContext ctx) {
    System.out.println(">>> visitConstructorCall triggered for: " + ctx.getText());

    String className = ctx.getText().split("\\.")[0].toLowerCase(); // Get "person" from "Person.Create"
    System.out.println("Constructor Call detected for class: " + className);

    // Print current fields stored for the class
    System.out.println("Fields in classFields for " + className + ": " + classFields.get(className));

    Instance newInstance = new Instance(className);

    if (classFields.containsKey(className)) {
        for (String field : classFields.get(className)) {
            System.out.println("Initializing field: " + field);
            newInstance.setField(field, null); // Default value is null
        }
    } else {
        System.out.println(" No classFields found for: " + className);
    }

    System.out.println("New instance created: " + newInstance);
    return newInstance;
}



    /**
     * Visit a constructor declaration (prototype) within a class.
     * @param ctx The ConstructorDeclContext.
     * @return null
     */
    @Override
    public Object visitConstructorDecl(delphiParser.ConstructorDeclContext ctx) {
        if (ctx.identifier() != null) {
            System.out.println("Constructor Declaration: " + ctx.identifier().getText());
        } else {
            System.out.println("Constructor declaration found, but ID is missing.");
        }
        return visitChildren(ctx);
    }

    /**
     * Visit a destructor declaration (prototype) within a class.
     * @param ctx The DestructorDeclContext.
     * @return null
     */
    @Override
    public Object visitDestructorDecl(delphiParser.DestructorDeclContext ctx) {
        if (ctx.identifier() != null) {
            System.out.println("Destructor Declaration: " + ctx.identifier().getText());
        } else {
            System.out.println("Destructor declaration found, but ID is missing.");
        }
        return visitChildren(ctx);
    }


    @Override
public Object visitString(delphiParser.StringContext ctx) {
    // Assuming ctx.STRING_LITERAL() returns the token with quotes.
    String text = ctx.STRING_LITERAL().getText();
    // Remove surrounding quotes and unescape if necessary.
    return text.substring(1, text.length() - 1);
}

@Override
public Object visitUnsignedInteger(delphiParser.UnsignedIntegerContext ctx) {
    // return Integer.parseInt(ctx.getText());  -3
    return ctx.getText();
}



@Override
public Object visitUnsignedNumber(delphiParser.UnsignedNumberContext ctx) {
    // If the text contains a decimal point, parse it as a double.
    if (ctx.getText().contains(".")) {
        return Double.parseDouble(ctx.getText());
    } else { // Otherwise, parse it as an integer.
        return Integer.parseInt(ctx.getText());
    }
}



    // --- Variable Declarations and Assignments ---

    /**
     * Visit a variable declaration.
     * Prints global variable declarations only.
     * @param ctx The VariableDeclarationContext.
     * @return null
     */
    @Override
    public Object visitVariableDeclaration(delphiParser.VariableDeclarationContext ctx) {
        // Avoid duplicate printing for variables declared inside a class.
        if (!(ctx.getParent() instanceof delphiParser.ClassMemberContext)) {
            if (ctx.identifierList() != null && ctx.type_() != null) {
                String varList = ctx.identifierList().getText();
                // Split and trim identifiers; then define each in the symbol table.
                for (String var : varList.split(",")) {
                    var = var.trim().toLowerCase();
                    currentScope.define(var, 0); // Or your default value based on type.
                    LLVMGenerator.declareVariable(var); // Allocate space in LLVM IR -3

                }
                System.out.println("Variable(s) declared: " 
                        + ctx.identifierList().getText() 
                        + " of type " + ctx.type_().getText());
            }
        }
        return visitChildren(ctx);
    }
    

    /**
     * Visit an assignment statement.
     * Echoes assignments and constructor calls.
     * @param ctx The AssignmentStatementContext.
     * @return null
     */
    // @Override
    // public Object visitAssignmentStatement(delphiParser.AssignmentStatementContext ctx) {
    //     if (ctx.variable() != null && ctx.expression() != null) {

    //         String varName = ctx.variable().getText();
    //     Object value = visit(ctx.expression());
    //     currentScope.assign(varName, value);

    //     System.out.println("Assignment: " + varName + " = " + value);

    //     } else if (ctx.constructorCall() != null) {
    //         System.out.println("Constructor Call: " + ctx.constructorCall().getText());
    //     } else {
    //         System.out.println("Invalid assignment statement: " + ctx.getText());
    //     }
    //     return visitChildren(ctx);
    // }

    // @Override
    // public Object visitSimpleExpression(delphiParser.SimpleExpressionContext ctx) {
    //     Object left = visit(ctx.term());
    
    //     if (ctx.additiveoperator() != null && ctx.simpleExpression() != null) {
    //         Object right = visit(ctx.simpleExpression());
    //         String op = ctx.additiveoperator().getText();
    
    //         if (left instanceof Integer && right instanceof Integer) {
    //             int l = (Integer) left;
    //             int r = (Integer) right;
    
    //             switch (op) {
    //                 case "+": return l + r;
    //                 case "-": return l - r;
    //                 case "OR": return (l != 0 || r != 0) ? 1 : 0;
    //                 default:
    //                     throw new RuntimeException("Unsupported additive operator: " + op);
    //             }
    //         } else {
    //             throw new RuntimeException("Unsupported operands: " + left + " " + op + " " + right);
    //         }
    //     }
    
    //     return left;
    // }
   

    @Override
public Object visitSimpleExpression(delphiParser.SimpleExpressionContext ctx) {
    Object leftObj = visit(ctx.term());

    if (ctx.additiveoperator() != null) {
        Object rightObj = visit(ctx.simpleExpression());
        String op = ctx.additiveoperator().getText();

        String left = leftObj.toString();
        String right = rightObj.toString();

        // ✅ Check if left is a variable (e.g., %x) and not a temp like %t3
        if (left.startsWith("%") && !left.startsWith("%t")) {
            left = LLVMGenerator.load(left.substring(1)); // remove '%' and load
        }

        if (right.startsWith("%") && !right.startsWith("%t")) {
            right = LLVMGenerator.load(right.substring(1)); // same here
        }

        String result = LLVMGenerator.nextTemp();

        switch (op) {
            case "+":
                LLVMGenerator.emit(result + " = add i32 " + left + ", " + right);
                break;
            case "-":
                LLVMGenerator.emit(result + " = sub i32 " + left + ", " + right);
                break;
            case "or":
            case "OR":
                LLVMGenerator.emit(result + " = or i32 " + left + ", " + right);
                break;
            default:
                throw new RuntimeException("Unsupported additive operator: " + op);
        }

        return result;
    }

    return leftObj;
}


    
    @Override
public Object visitAssignmentStatement(delphiParser.AssignmentStatementContext ctx) {
  
    // String varText = null;
    // if (ctx.variable() != null) {
    //     varText = ctx.variable().getText().trim().toLowerCase();
    //     System.out.println("lhs:" + varText);
    // } else if (ctx.identifier() != null && !ctx.identifier().isEmpty()) {
    //     varText = ctx.identifier(0).getText().trim().toLowerCase();
    //     System.out.println("lhs:" + varText);
    // } else {
    //     System.out.println("Invalid assignment statement: " + ctx.getText());
    //     return null;
    // }
    
    // System.out.println(">>> Assignment LHS: " + varText);
    // Object value = this.visit(ctx.expression());
    // System.out.println(">>> Assignment RHS (value): " + value);

    // if (varText.contains(".")) { // Field assignment
    //     String[] parts = varText.split("\\.");
    //     if (parts.length != 2) {
    //         throw new RuntimeException("Invalid field access syntax: " + varText);
    //     }
    //     String objName = parts[0];
    //     String fieldName = parts[1];
    //     Object obj = currentScope.resolve(objName);
    //     if (!(obj instanceof Instance)) {
    //         throw new RuntimeException("Variable " + objName + " is not an object instance.");
    //     }
    //     ((Instance) obj).setField(fieldName, value);
    //     System.out.println(">>> Field Assignment: " + objName + "." + fieldName + " = " + value);
    // } else {
    //     currentScope.assign(varText, value);
    //     System.out.println(">>> Assignment: " + varText + " = " + value);
    // }
    // return null;



    String varName = ctx.identifier(0).getText().toLowerCase();
    Object exprResult = visit(ctx.expression());

    if (exprResult == null) {
        throw new RuntimeException("Right-hand side of assignment returned null for: " + ctx.expression().getText());
    }

    String value = exprResult.toString(); // Should be LLVM register like %tX or constant

    if (value.startsWith("%") && !value.matches("%t\\d+")) {
        // This means it's a variable like %x, not a temp like %t1
        value = LLVMGenerator.load(value.substring(1));
    }
    
    if (!LLVMGenerator.isTemp(value) && value.startsWith("%")) {
        value = LLVMGenerator.load(value.substring(1));
    }
        LLVMGenerator.store(value, varName);
    
    return null;

}

    
//     @Override
// public Object visitAssignmentStatement(delphiParser.AssignmentStatementContext ctx) {
//     if (ctx.constructorCall() != null) {
//         // Example: p := Person.CREATE();
//         String varName = ctx.identifier(0).getText().toLowerCase();
//         System.out.println("Constructor assignment to: " + varName);

//         Object value = visit(ctx.constructorCall());

//         currentScope.assign(varName, value);

//         if (value instanceof Instance) {
//             System.out.println(">>> Object Assignment: " + varName + " = " + value);
//         } else {
//             System.out.println(">>> Assignment RHS (not object): " + value);
//         }
//         return null;
//     }

//     String varText = ctx.variable() != null
//     ? ctx.variable().getText().toLowerCase()
//     : ctx.identifier(0).getText().toLowerCase();
//     System.out.println(">>> Assignment LHS: " + varText);

//     Object value = visit(ctx.expression());
//     System.out.println(">>> Assignment RHS (value): " + value);

//     if (varText.contains(".")) {
//         String[] parts = varText.split("\\.");
//         if (parts.length != 2) {
//             throw new RuntimeException("Invalid field access syntax: " + varText);
//         }

//         String objName = parts[0];
//         String fieldName = parts[1];

//         Object obj = currentScope.resolve(objName);
//         if (!(obj instanceof Instance)) {
//             throw new RuntimeException("Variable " + objName + " is not an object instance.");
//         }

//         ((Instance) obj).setField(fieldName.toLowerCase(), value);
//         System.out.println(">>> Field Assignment: " + objName + "." + fieldName + " = " + value);
//     } else {
//         currentScope.assign(varText, value);
//         System.out.println(">>> Assignment: " + varText + " = " + value);
//     }

//     return null;
// }

    



    // @Override
    // public Object visitExpression(delphiParser.ExpressionContext ctx) {
    //     // Check if it's a comparison (e.g., x <= 3)
    //     if (ctx.relationaloperator() != null) {
    //         Object left = visit(ctx.simpleExpression());
    //         Object right = visit(ctx.expression());
    //         String op = ctx.relationaloperator().getText();
    
    //         if (left instanceof Integer && right instanceof Integer) {
    //             int l = (Integer) left;
    //             int r = (Integer) right;
    //             switch (op) {
    //                 case "=": return l == r;
    //                 case "<>": return l != r;
    //                 case "<": return l < r;
    //                 case "<=": return l <= r;
    //                 case ">": return l > r;
    //                 case ">=": return l >= r;
    //             }
    //         } else if (left instanceof Number && right instanceof Number) {
    //             double l = ((Number) left).doubleValue();
    //             double r = ((Number) right).doubleValue();
    //             switch (op) {
    //                 case "=": return l == r;
    //                 case "<>": return l != r;
    //                 case "<": return l < r;
    //                 case "<=": return l <= r;
    //                 case ">": return l > r;
    //                 case ">=": return l >= r;
    //             }
    //         } else {
    //             throw new RuntimeException("Unsupported types for comparison: " + left + " and " + right);
    //         }
    //     }
    
    //     // No comparison, just evaluate the simple expression
    //     return visit(ctx.simpleExpression());
    // } --3

    @Override
    public Object visitExpression(delphiParser.ExpressionContext ctx) {
        //  Check if the expression is a function call like: add(a, b)
        if (ctx.simpleExpression() != null && ctx.simpleExpression().getChildCount() >= 3) {
            ParseTree first = ctx.simpleExpression().getChild(0);
            ParseTree second = ctx.simpleExpression().getChild(1);
            ParseTree third = ctx.simpleExpression().getChild(2);
    
            if (first != null && second.getText().equals("(")) {
                String funcName = first.getText().toLowerCase();
    
                // Look up the function definition
                Object resolved = currentScope.resolve(funcName);
                if (!(resolved instanceof Procedure)) {
                    throw new RuntimeException("Function '" + funcName + "' not found.");
                }
                Procedure func = (Procedure) resolved;
    
                // Extract arguments
                List<Object> args = new ArrayList<>();
                for (int i = 2; i < ctx.simpleExpression().getChildCount() - 1; i += 2) { // skips "(" and ")"
                    Object arg = visit(ctx.simpleExpression().getChild(i));
                    args.add(arg);
                }
    
                // Set up new scope and define parameters
                Scope funcScope = new Scope(currentScope);
                for (int i = 0; i < args.size(); i++) {
                    String paramName = func.getParameters().get(i);
                    Object argValue = args.get(i);
                    funcScope.define(paramName, argValue);
                    LLVMGenerator.declareVariable(paramName);
                    LLVMGenerator.store(argValue.toString(), paramName);
                }
    
                // Call the function
                Scope prevScope = currentScope;
                currentScope = funcScope;
                func.call(args, funcScope, this);
                currentScope = prevScope;
    
                // Load and return the result (stored in variable named after the function)
                return LLVMGenerator.load(funcName);
            }
        }
    
        //  Handle comparisons (already implemented)
        if (ctx.relationaloperator() != null) {
            String left = visit(ctx.simpleExpression()).toString();
            String right = visit(ctx.expression()).toString();
            String op = ctx.relationaloperator().getText();
    
            if (left.startsWith("%") && !left.startsWith("%t")) {
                left = LLVMGenerator.load(left.substring(1));
            }
            if (right.startsWith("%") && !right.startsWith("%t")) {
                right = LLVMGenerator.load(right.substring(1));
            }
    
            String llvmOp = switch (op) {
                case "=" -> "eq";
                case "<>" -> "ne";
                case "<" -> "slt";
                case "<=" -> "sle";
                case ">" -> "sgt";
                case ">=" -> "sge";
                default -> throw new RuntimeException("Unsupported operator: " + op);
            };
    
            String result = LLVMGenerator.nextTemp();
            LLVMGenerator.emit(result + " = icmp " + llvmOp + " i32 " + left + ", " + right);
            LLVMGenerator.setLastCondition(result);
            return result;
        }
    
        //  Fallback
        return visit(ctx.simpleExpression());
    }
    











    // --- Procedure and Method Calls ---

    /**
     * Visit a procedure declaration outside of a class.
     * @param ctx The ProcedureDeclarationContext.
     * @return null
     */


    // @Override
    // public Void visitProcedureDeclaration(delphiParser.ProcedureDeclarationContext ctx) {

        

    //     // Print only global procedure declarations.
    //     if (!(ctx.getParent() instanceof delphiParser.ClassMemberContext)) {
    //         if (ctx.identifier() != null && ctx.identifier().size() >= 2) {
    //             System.out.println("Procedure Declaration: " +
    //                 ctx.identifier(0).getText() + "." + ctx.identifier(1).getText());
    //         } else {
    //             System.out.println("Procedure Declaration: " + ctx.getText());
    //         }
    //     }
    //     return visitChildren(ctx);
    // }



    //p2

    @Override
    public Object visitProcedureDeclaration(delphiParser.ProcedureDeclarationContext ctx) {
        List<String> parameters = new ArrayList<>();
        if (ctx.formalParameterList() != null) {
            for (delphiParser.FormalParameterSectionContext section : ctx.formalParameterList().formalParameterSection()) {
                if (section.parameterGroup() != null) {
                    for (delphiParser.IdentifierContext idCtx : section.parameterGroup().identifierList().identifier()) {
                        parameters.add(idCtx.getText().toLowerCase());
                    }
                }
            }
        }
    
        delphiParser.BlockContext body = ctx.block();
        Procedure proc = new Procedure(parameters, body);
    
        if (ctx.identifier().size() >= 2) {
            // Class method
            String className = ctx.identifier().get(0).getText().toLowerCase();
            String methodName = ctx.identifier().get(1).getText().toLowerCase();
            String fullKey = className + "." + methodName;
            currentScope.define(fullKey, proc);
            System.out.println("Class Method Declaration stored: " + fullKey + " with parameters: " + parameters);
        } else if (ctx.identifier().size() == 1) {
            // Global procedure
            String procName = ctx.identifier().get(0).getText().toLowerCase();
            currentScope.define(procName, proc);
            System.out.println("Procedure Declaration stored: " + procName + " with parameters: " + parameters);
    
            // ✅ Use a custom buffer to emit code cleanly
            StringBuilder procCode = new StringBuilder();
            LLVMGenerator.pushCustomBuffer(procCode);
    
            LLVMGenerator.emit("define void @" + procName + "() {");
            LLVMGenerator.emit("entry:");
    
            pushScope();
            visit(body);
            popScope();
    
            LLVMGenerator.emit("ret void");
            LLVMGenerator.emit("}");
    
            LLVMGenerator.popCustomBuffer(); // pops the buffer you just filled
    
            // ✅ Store this code in the top-level procedure section
            LLVMGenerator.emitProcedure(procCode.toString());
        } else {
            System.out.println("Procedure Declaration could not be parsed properly: " + ctx.getText());
        }
    
        return null;
    }
    
    
    
    






//     @Override
// public Void visitWhileStatement(delphiParser.WhileStatementContext ctx) {
//     while (true) {
//         Object condition = visit(ctx.expression());
//         System.out.println(">>> WHILE condition evaluates to: " + condition);

//         if (!(condition instanceof Boolean)) {
//             throw new RuntimeException("WHILE condition is not boolean: " + condition);
//         }

//         if (!((Boolean) condition)) {
//             break;
//         }

//         // 💡 Don't push a new scope unless you're defining loop-local vars
//         try {
//             visit(ctx.statement());
//         } catch (BreakException be) {
//             break;
//         } catch (ContinueException ce) {
//             // continue
//         }
//     }
//     return null;
// }  -3


// @Override
// public Void visitWhileStatement(delphiParser.WhileStatementContext ctx) {
//     String condLabel = LLVMGenerator.newLabel();
//     String bodyLabel = LLVMGenerator.newLabel();
//     String endLabel = LLVMGenerator.newLabel();

//     // Entry to condition check
//     LLVMGenerator.branch(condLabel);

//     LLVMGenerator.label(condLabel);

//     Object condValue = visit(ctx.expression());
//     if (!(condValue instanceof Boolean)) {
//         throw new RuntimeException("WHILE condition is not boolean");
//     }

//     // Just for interpretation output
//     System.out.println(">>> WHILE condition evaluates to: " + condValue);

//     String condTemp = LLVMGenerator.lastCondition(); // load+icmp already emitted inside visitExpression
//     LLVMGenerator.conditionalBranch(condTemp, bodyLabel, endLabel);

//     LLVMGenerator.label(bodyLabel);
//     try {
//         visit(ctx.statement());
//     } catch (BreakException be) {
//         LLVMGenerator.branch(endLabel);
//     } catch (ContinueException ce) {
//         LLVMGenerator.branch(condLabel);
//     }

//     LLVMGenerator.branch(condLabel);
//     LLVMGenerator.label(endLabel);

//     return null;
// } --3


  @Override
public Void visitWhileStatement(delphiParser.WhileStatementContext ctx) {
    String loopLabel = LLVMGenerator.nextLabel("loop");
    String bodyLabel = LLVMGenerator.nextLabel("body");
    String endLabel = LLVMGenerator.nextLabel("end");

    // Jump to loop condition
    LLVMGenerator.emit("br label %" + loopLabel);
    LLVMGenerator.emit(loopLabel + ":");

    // Evaluate condition expression (e.g., x < 5)
    Object conditionResult = visit(ctx.expression());  // emits icmp inside visitExpression()
    String condTemp = conditionResult.toString();      // e.g., %t5

    LLVMGenerator.emit("br i1 " + condTemp + ", label %" + bodyLabel + ", label %" + endLabel);

    // Loop body
    LLVMGenerator.emit(bodyLabel + ":");
    visit(ctx.statement());

    // Go back to loop condition
    LLVMGenerator.emit("br label %" + loopLabel);

    // Loop exit label
    LLVMGenerator.emit(endLabel + ":");

    return null;
}














    /**
     * Visit a procedure statement (i.e., a procedure call).
     * Distinguishes between built-in procedures like "writeln" and qualified method calls.
     * @param ctx The ProcedureStatementContext.
     * @return null
     */

     @Override
public Void visitProcedureStatement(delphiParser.ProcedureStatementContext ctx) {
    if (ctx.identifier() != null && !ctx.identifier().isEmpty()) {
        // Handle unqualified procedure calls
        if (ctx.identifier().size() == 1) {
            String procName = ctx.identifier().get(0).getText().toLowerCase();

            if ("writeln".equals(procName)) {
                System.out.println(">>> visitProcedureStatement detected writeln");

                if (ctx.parameterList() != null) {
                    for (delphiParser.ActualParameterContext paramCtx : ctx.parameterList().actualParameter()) {
                        String raw = paramCtx.getText();

                        if (raw.startsWith("'") && raw.endsWith("'")) {
                            // String literal
                            String unquoted = raw.substring(1, raw.length() - 1);
                            String strLabel = LLVMGenerator.defineStringConstant(unquoted);
                            LLVMGenerator.printString(strLabel, unquoted.length() + 1);
                            System.out.println("LLVM emitted string print for: " + strLabel);
                        } else {
                            // Variable name
                            String varName = raw.toLowerCase();
                            String loaded = LLVMGenerator.load(varName);
                            LLVMGenerator.print(loaded);
                            System.out.println("LLVM emitted int print for: " + loaded);
                        }
                    }
                }

            } else if ("readln".equals(procName)) {
                System.out.print("Enter a number: ");
                int input = this.scanner.nextInt();
                System.out.println("You entered: " + input);
            } else {
                // User-defined procedure
                try {
                    Procedure proc = (Procedure) currentScope.resolve(procName);
                    List<Object> args = new ArrayList<>();
                    if (ctx.parameterList() != null) {
                        for (delphiParser.ActualParameterContext paramCtx : ctx.parameterList().actualParameter()) {
                            args.add(visit(paramCtx));
                        }
                    }
                    proc.call(args, currentScope, this);
                    System.out.println("Procedure Call: " + procName);
                    LLVMGenerator.emit("call void @" + procName + "()");
                } catch (Exception e) {
                    System.out.println("Procedure Call Error: Procedure '" + procName + "' not found.");
                }
            }

        } else if (ctx.identifier().size() >= 2) {
            // Qualified method call (e.g., obj.method)
            String objectName = ctx.identifier().get(0).getText().trim().toLowerCase();
            String methodName = ctx.identifier().get(1).getText().trim().toLowerCase();

            Object obj = currentScope.resolve(objectName);
            if (!(obj instanceof Instance)) {
                throw new RuntimeException("Variable " + objectName + " is not an object instance.");
            }
            Instance instance = (Instance) obj;

            String className = instance.getClassName();
            String methodKey = className + "." + methodName;

            Procedure methodProc = (Procedure) currentScope.resolve(methodKey);

            List<Object> args = new ArrayList<>();
            Scope methodScope = new Scope(currentScope);
            methodScope.define("self", instance);

            Instance prevSelf = currentSelf;
            Scope prevScope = getCurrentScope();

            currentSelf = instance;
            setCurrentScope(methodScope);

            methodProc.call(args, methodScope, this);

            setCurrentScope(prevScope);
            currentSelf = prevSelf;
        }
    }
    return null;
}













    // @Override
    // public Void visitProcedureStatement(delphiParser.ProcedureStatementContext ctx) {
    //     if (ctx.identifier() != null && !ctx.identifier().isEmpty()) {
    //         // Unqualified procedure call (e.g., writeln)
    //         if (ctx.identifier().size() == 1) {
    //             String procName = ctx.identifier(0).getText();
    //             if ("writeln".equalsIgnoreCase(procName)) {
    //                 System.out.println("Procedure Call: " + procName);
    //                 if (ctx.parameterList() != null) {
    //                     System.out.println("Parameters: " + ctx.parameterList().getText());
    //                 }
    //             } else if ("readln".equalsIgnoreCase(procName)) {
    //                 System.out.print("Enter a number: ");
    //                 int input = this.scanner.nextInt();
    //                 System.out.println("You entered: " + input);
    //             } else {
    //                 System.out.println("Procedure Call: " + procName);
    //             }
    //         } 
    //         // Qualified method call (e.g., p.Introduce)
    //         else if (ctx.identifier().size() >= 2) {
    //             String objectName = ctx.identifier(0).getText();
    //             String methodName = ctx.identifier(1).getText();
    //             System.out.println("Method Call Detected: Object `" + objectName +
    //                                "` is calling method `" + methodName + "`");
    //         }
    //     } 
    //     // Fallback: If identifiers aren’t captured as expected, split by dot.
    //     else if (ctx.getText().contains(".")) {
    //         String text = ctx.getText();
    //         String[] parts = text.split("\\.");
    //         if (parts.length >= 2) {
    //             String objectName = parts[0].trim();
    //             String methodPart = parts[1].trim();
    //             if (methodPart.endsWith(";")) {
    //                 methodPart = methodPart.substring(0, methodPart.length() - 1).trim();
    //             }
    //             System.out.println("Method Call Detected: Object `" + objectName +
    //                                "` is calling method `" + methodPart + "`");
    //         } else {
    //             System.out.println("Procedure Call (fallback): " + ctx.getText());
    //         }
    //     } else {
    //         System.out.println("Procedure Call: " + ctx.getText());
    //     }
    //     return visitChildren(ctx);
    // }





//p2 


// @Override
// public Object visitVariable(delphiParser.VariableContext ctx) {
//     String varText = ctx.getText().trim();
//     String varName = varText.toLowerCase();

//     System.out.println("Resolving variable: '" + varText + "'");

//     // Field access like obj.field
//     if (varText.contains(".")) {
//         String[] parts = varText.split("\\.");
//         if (parts.length == 2) {
//             String objName = parts[0].toLowerCase();
//             String fieldName = parts[1].toLowerCase();
//             Object obj = currentScope.resolve(objName);
//             if (obj instanceof Instance) {
//                 Instance inst = (Instance) obj;
//                 return inst.getField(fieldName);
//             }
//         }
//     }

//     // Regular variable
//     try {
//         return currentScope.resolve(varName);
//     } catch (RuntimeException e) {
//         // Instead of resolving "self" from scope, use currentSelf directly
//         if (currentSelf != null) {
//             System.out.println("  currentSelf is set, checking for field: " + varName);
//             if (currentSelf.hasField(varName)) {
//                 return currentSelf.getField(varName);
//             } else {
//                 System.out.println("  Field '" + varName + "' not found in currentSelf.");
//             }
//         } else {
//             System.out.println("  currentSelf is null.");
//         }

//         throw new RuntimeException("Undefined symbol: " + varText);
//     }
// }



@Override
public Object visitVariable(delphiParser.VariableContext ctx) {
    String varName = ctx.getText().toLowerCase();
    return "%" + varName;  // Return the LLVM pointer name, e.g., %x
}



// @Override
// public Void visitIfStatement(delphiParser.IfStatementContext ctx) {
//     String thenLabel = LLVMGenerator.nextLabel("then");
//     String elseLabel = ctx.statement().size() > 1 ? LLVMGenerator.nextLabel("else") : null;
//     String endLabel = LLVMGenerator.nextLabel("endif");

//     String cond = visit(ctx.expression()).toString(); // %tX (should be i1)

//     if (elseLabel != null) {
//         LLVMGenerator.brCond(cond, thenLabel, elseLabel);
//     } else {
//         LLVMGenerator.brCond(cond, thenLabel, endLabel);
//     }

//     LLVMGenerator.label(thenLabel);
//     visit(ctx.statement(0)); // THEN part
//     LLVMGenerator.br(endLabel);

//     if (elseLabel != null) {
//         LLVMGenerator.label(elseLabel);
//         visit(ctx.statement(1)); // ELSE part
//         LLVMGenerator.br(endLabel);
//     }

//     LLVMGenerator.label(endLabel);
//     return null;
// }

@Override
public Void visitIfStatement(delphiParser.IfStatementContext ctx) {
    String thenLabel = LLVMGenerator.nextLabel("then");
    String elseLabel = (ctx.statement().size() > 1) ? LLVMGenerator.nextLabel("else") : null;
    String endLabel = LLVMGenerator.nextLabel("endif");

    // Generate the condition code
    String cond = visit(ctx.expression()).toString(); // %tX (should be i1)

    // Emit conditional branch
    if (elseLabel != null) {
        LLVMGenerator.brCond(cond, thenLabel, elseLabel);
    } else {
        LLVMGenerator.brCond(cond, thenLabel, endLabel);
    }

    // THEN block
    LLVMGenerator.label(thenLabel);
    visit(ctx.statement(0));
    LLVMGenerator.br(endLabel);

    // ELSE block (only if it exists)
    if (elseLabel != null) {
        LLVMGenerator.label(elseLabel);
        visit(ctx.statement(1));
        LLVMGenerator.br(endLabel);
    }

    // This ensures the target label is **always defined**
    LLVMGenerator.label(endLabel);

    return null;
}










    // --- Compound Statement (Main Executable Block) ---

    /**
     * Visit a compound statement.
     * Indicates entry into a block of executable statements.
     * @param ctx The CompoundStatementContext.
     * @return null
     */
    @Override
    public Void visitCompoundStatement(delphiParser.CompoundStatementContext ctx) {
        System.out.println("Entering compound statement...");
          pushScope(); // Create a new scope for the block.
            visitChildren(ctx);
            popScope();  // Exit the block.
            return null;
    }
    
    /**
     * Fallback to visit children nodes.
     * @param node The RuleNode.
     * @return The result of visiting children.
     */
    @Override
    public Object visitChildren(RuleNode node) {
        return super.visitChildren(node);
    }



    @Override
    public Object visitBreakStatement(delphiParser.BreakStatementContext ctx) {
        if (breakLabelStack.isEmpty()) {
            throw new RuntimeException("BREAK used outside of a loop");
        }
        LLVMGenerator.br(breakLabelStack.peek());
        return null;
    }
    
    @Override
    public Object visitContinueStatement(delphiParser.ContinueStatementContext ctx) {
        if (continueLabelStack.isEmpty()) {
            throw new RuntimeException("CONTINUE used outside of a loop");
        }
        LLVMGenerator.br(continueLabelStack.peek());
        return null;
    }


    @Override
    public Object visitFactor(delphiParser.FactorContext ctx) {
        if (ctx.functionDesignator() != null) {
            return visitFunctionDesignator(ctx.functionDesignator());
        }
        return visitChildren(ctx);  // <-- add this as fallback
    }
    




    @Override
    public Object visitFunctionDesignator(delphiParser.FunctionDesignatorContext ctx) {
        String funcName = ctx.identifier().getText().toLowerCase();
        List<Object> args = new ArrayList<>();
    
        // Collect arguments
        if (ctx.parameterList() != null) {
            for (delphiParser.ActualParameterContext paramCtx : ctx.parameterList().actualParameter()) {
                args.add(visit(paramCtx));
            }
        }
    
        // Lookup function definition
        Object resolved = currentScope.resolve(funcName);
        if (!(resolved instanceof Procedure)) {
            throw new RuntimeException("Function '" + funcName + "' is not defined.");
        }
        Procedure func = (Procedure) resolved;
    
        // Create new scope for the function call
        Scope funcScope = new Scope(currentScope);
        List<String> paramNames = func.getParameters();
        if (args.size() != paramNames.size()) {
            throw new RuntimeException("Function '" + funcName + "' expects " + paramNames.size() + " arguments but got " + args.size());
        }
    
        // Declare function result variable before using it
        LLVMGenerator.declareVariable(funcName); //  Declare before storing/loading result
    
        // Inject parameters into function scope
        for (int i = 0; i < args.size(); i++) {
            String paramName = paramNames.get(i);
            Object argValue = args.get(i);
            funcScope.define(paramName, argValue);
            LLVMGenerator.declareVariable(paramName);
    
            String argValStr = argValue.toString();
            if (argValStr.startsWith("%") && !argValStr.startsWith("%t")) {
                argValStr = LLVMGenerator.load(argValStr.substring(1));
            }
            LLVMGenerator.store(argValStr, paramName);
        }
    
        // Call the function
        Scope prevScope = currentScope;
        currentScope = funcScope;
        func.call(args, funcScope, this);
        currentScope = prevScope;
    
        // Load and return result
        String resultTemp = LLVMGenerator.load(funcName);
        return resultTemp;
    }
    




}
