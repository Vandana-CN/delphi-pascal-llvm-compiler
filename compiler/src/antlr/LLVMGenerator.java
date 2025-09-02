package antlr;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;


public class LLVMGenerator {
    private static Set<String> declaredVariables = new HashSet<>();

  
    private static int tempCount = 1;

    private static int labelCount = 0;
    private static String lastCond = "";

    private static int stringCount = 0;

    private static StringBuilder procedures = new StringBuilder();

    private static StringBuilder globalDefs = new StringBuilder();  // For global strings, etc.

    private static StringBuilder globalCode = new StringBuilder();
    private static StringBuilder mainCode = new StringBuilder();
    private static Deque<StringBuilder> bufferStack = new ArrayDeque<>();
    private static StringBuilder code = mainCode;


    public static void pushCustomBuffer(StringBuilder buffer) {
        bufferStack.push(code);
        code = buffer;
    }
    
    public static void popCustomBuffer() {
        code = bufferStack.pop();
    }

    public static void emitGlobal(String line) {
        globalCode.append(line);
    }
   
    public static void emitProcedure(String line) {
        procedures.append(line).append("\n");  // used for greet, etc.
    }
    
    public static boolean isTemp(String s) {
        return s.matches("%t\\d+");
    }
    

    public static void declareVariable(String varName) {
        if (!declaredVariables.contains(varName)) {
            code.append("%" + varName + " = alloca i32\n");
            declaredVariables.add(varName);
        }
    }    
    
    public static String defineStringConstant(String value) {
        String name = "@.str" + stringCount++;
        int len = value.length() + 1;
        String escaped = value.replace("\"", "\\22").replace("\n", "\\0A") + "\\00";
        globalDefs.append(name + " = constant [" + len + " x i8] c\"" + escaped + "\"\n");
        return name;
    }
    
    

    public static void printString(String strLiteralName, int len) {
        emit("call i32 (i8*, ...) @printf(i8* getelementptr ([" + len + " x i8], [" + len + " x i8]* " + strLiteralName + ", i32 0, i32 0))");
    }
    

    public static void startProgram() {

        mainCode.append("define i32 @main() {\n");
        mainCode.append("entry:\n");
        code = mainCode;
    }
    
    
    public static void endProgram() {
        mainCode.append("ret i32 0\n}\n");
    }

    public static String add(int a, int b) {
        String result = "%t" + tempCount++;
        code.append(result + " = add i32 " + a + ", " + b + "\n");
        return result;
    }

    public static void store(String from, String to) {
        code.append("store i32 " + from + ", i32* %" + to + "\n");
    }

    public static void printLoaded(String varName) {
        String tmp = "%t" + tempCount++;
        code.append(tmp + " = load i32, i32* %" + varName + "\n");
        code.append("call i32 (i8*, ...) @printf(i8* getelementptr " +
            "([4 x i8], [4 x i8]* @print.str, i32 0, i32 0), i32 " + tmp + ")\n");
    }

    public static void writeToFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // Emit declarations and global strings at the top
            writer.write("declare i32 @printf(i8*, ...)\n");
            writer.write("@print.str = constant [4 x i8] c\"%d\\0A\\00\"\n");
            writer.write(globalDefs.toString());
    
            // Emit user-defined procedures (like greet, add)
            writer.write(procedures.toString());
    
            // WRITE MAIN CODE DIRECTLY (already has full main function)
            writer.write(mainCode.toString());
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    
    
    
    

    



  public static String nextTemp() {
    return "%t" + tempCount++;
}

    

    public static String load(String varName) {
        String temp = nextTemp();
        code.append(temp + " = load i32, i32* %" + varName + "\n");
        return temp;
    }
    
    public static void print(String value) {
        code.append("call i32 (i8*, ...) @printf(i8* getelementptr " +
            "([4 x i8], [4 x i8]* @print.str, i32 0, i32 0), i32 " + value + ")\n");
    }

    public static String newLabel() {
        return "L" + tempCount++;
    }

    public static String newLabel(String base) {
        return base + labelCount++;
    }
    
    
    public static void label(String label) {
        code.append(label).append(":\n");
    }
    
    public static void br(String label) {
        code.append("br label %").append(label).append("\n");
    }
    
    public static void brCond(String cond, String trueLabel, String falseLabel) {
        code.append("br i1 ").append(cond).append(", label %").append(trueLabel)
            .append(", label %").append(falseLabel).append("\n");
    }
    
    public static String icmp(String op, String left, String right) {
        String temp = nextTemp();
        code.append(temp + " = icmp " + op + " i32 " + left + ", " + right + "\n");
        return temp;
    }

    public static String add(String a, String b) {
        String result = nextTemp();
        code.append(result + " = add i32 " + a + ", " + b + "\n");
        return result;
    }

    
    public static void branch(String label) {
        code.append("br label %" + label + "\n");
    }
    
    public static void conditionalBranch(String condVar, String trueLabel, String falseLabel) {
        code.append("br i1 " + condVar + ", label %" + trueLabel + ", label %" + falseLabel + "\n");
    }
    
    // Set the last comparison result (used in while/if)
    public static void setLastCondition(String name) {
        lastCond = name;
    }
    
    public static String lastCondition() {
        return lastCond;
    }
    
    
    public static void emit(String line) {
        code.append(line).append("\n");
    }
     
    public static String nextLabel() {
        return "L" + (labelCount++);
    }
    
    public static String nextLabel(String base) {
        return base + (tempCount++);
    }
    
    
}
