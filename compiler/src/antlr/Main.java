package antlr;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import antlr.delphiLexer;
import antlr.delphiParser;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        try {
            // Ensure a file name is provided as an argument
            if (args.length == 0) {
                System.err.println("Usage: java -cp \"bin;antlr-4.9.3-complete.jar\" antlr.Main <filename.pas>");
                return;
            }

            // Read the provided file
            String filePath = args[0];
            String delphiCode = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);

            // Create an ANTLR input stream
            CharStream input = CharStreams.fromString(delphiCode);

            // Create a lexer and parser
            delphiLexer lexer = new delphiLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            delphiParser parser = new delphiParser(tokens);

            // Set up error listener to catch syntax errors
            parser.removeErrorListeners();
            parser.addErrorListener(new ErrorListener());

            // Parse the program
            ParseTree tree = parser.program();

            // Use DelphiVisitorImpl to walk through the tree
            DelphiVisitorImpl visitor = new DelphiVisitorImpl();
            visitor.visit(tree);

            // Final message (no parse tree dump is printed)
            System.out.println("\nParsing completed successfully!");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Custom Error Listener for better debugging
    static class ErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine, String msg, RecognitionException e) {
            System.err.println("Syntax Error at line " + line + ":" + charPositionInLine + " -> " + msg);
        }
    }
}
