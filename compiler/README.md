Delphi Interpreter Project
--------------------------

## Overview
This project extends our previous work from Project 1 (object-oriented features) and Project 2 (loops, procedures/functions, static scoping) by compiling a subset of Pascal/Delphi into LLVM Intermediate Representation (IR) instead of interpreting it directly.

The goal was to implement at least 70% of the Pascal/Delphi language features, and this project meets that requirement by compiling control flow, user-defined procedures/functions, basic expressions, and scoping into valid LLVM IR.
------------------------------------------


## How to Build and Run -

1. Extract the ZIP file.

2. Open the "Project1" folder.

3. Run the following commands from within the "Project1" directory:

	1. Generate the Parser and Lexer:

		java -jar antlr-4.9.3-complete.jar -Dlanguage=Java -visitor -package antlr -o src/antlr delphi.g4

		This command generates the parser, lexer, and visitor files in the src/antlr/ directory.
		///

	2. Compile the Java Source Files:

		javac -cp antlr-4.9.3-complete.jar -d bin src/antlr/*.java

		This compiles all the Java files and places the output in the bin/ directory.
	///

	3. Run the Interpreter:

		To run interpreter on a test case (for example, test1.pas), execute:

		java -cp "bin;antlr-4.9.3-complete.jar" antlr.Main tests/test1.pas

	Replace tests/test1.pas with tests/test2.pas to run the second test case.

	4. LLVM IR Output Files:

		- All generated .ll files corresponding to the test cases are saved inside the tests/ folder.
		- If you want to regenerate the LLVM IR files, simply run the script:

		./generate-ll.ps1
	5. Check the output of the test case:
		- View the output of each testcase by running the following command - lli tests/test6.ll
--------------------------------------------------
## Video Demonstration
[Click to watch](https://drive.google.com/file/d/1a9zlb7rCghyjwszd41NsgCgI3dz0p81f/view?usp=drive_link)	

## Features Implemented -

1.  Classes and Objects:
	Support for class declarations, constructors, methods, and object field access is carried over from Project 1. Objects are instantiated via constructor calls, and encapsulation is maintained during method execution using self scoping.

2.	Loop Constructs:
	The compiler generates LLVM IR for both while-do and for-do loops. Loop control is enhanced by the correct handling of break and continue statements, which are translated to appropriate conditional branches and labels in LLVM.

3.	User-Defined Procedures and Functions:
	Procedures (no return value) and functions (return via assignment to function name) are supported. Parameters are passed and stored using LLVM variable declarations and store instructions. Function return values are accessed by loading the variable named after the function.

4.	Static Scoping:
	Proper lexical scoping is implemented by creating new scope frames during block, loop, and function/procedure entry. Variable resolution respects this nesting, ensuring that each identifier is resolved in the correct context.

5.	LLVM IR Generation:
	All language features listed above are compiled into valid LLVM Intermediate Representation. The code generator emits IR for variable allocation (alloca), assignment (store), retrieval (load), arithmetic (add), printing (via printf), and function calls (call). Separate buffers are used to organize procedure/function definitions and the main program.



---------------------------------------------

## Files Included -

1. delphi.g4:
	The ANTLR4 grammar file for the extended Delphi language. This grammar supports constructs for classes, object-oriented features, loops (while-do, for-do), procedure/function declarations and calls, break, continue, and static scoping.

2. Main.java:
	The entry point of the compiler. It takes a .pas source file, tokenizes and parses it using ANTLR, and uses a visitor (DelphiVisitorImpl) to walk the parse tree and emit LLVM IR into output.ll.

3. DelphiVisitorImpl.java:
	The core visitor that performs code generation. This class now emits LLVM IR instructions (rather than interpreting) for all supported language features including loops, control flow, scoping, functions, and procedure calls.

4.	LLVMGenerator.java
	A helper class responsible for building LLVM IR. It provides methods for emitting alloca, store, load, arithmetic operations, branching, function definitions, and printing. It manages multiple code sections (globals, procedures, main).

5. Scope.java:
	Implements static lexical scoping for the compiler. It handles nested blocks and enables correct resolution of variables, procedure/function names, and object fields.

6. Instance.java:
	Represents instantiated class objects. Used to manage object field storage and method calls, maintaining encapsulation.

7. Procedure.java:
	Represents both procedures and functions. Manages parameter binding, function return behavior (via assignment to the function name), and scoped execution.

8. BreakException.java:
	Custom runtime exception used internally to handle BREAK logic within loop constructs during visitor traversal.

9. ContinueException.java:
	Custom runtime exception used internally to implement the CONTINUE statement inside loop bodies.

10. Test1.pas:
	Contains a FOR-DO loop printing numbers from 1 to 5. Tests basic loop and variable support.

11. Test2.pas:
	Contains a WHILE-DO loop that increments a variable from 1 to 5, testing conditionals and assignment.

12. Test3.pas:
	Tests loop control with BREAK and CONTINUE. A FOR-DO loop prints selected values from 1 to 10 while skipping and terminating based on conditions.


13. Test4.pas:
	Demonstrates procedures and functions. Includes a greeting procedure and a sum function that is called and printed.

14.	Test5.pas
	Demonstrates the IF-THEN-ELSE conditional construct. Depending on the condition, it prints either x or y. Useful for verifying comparison logic and control flow.

15.	Test6.pas
	A minimal example that tests integer addition and output. It adds two numbers and prints the result using writeln.


16. README.md:
	This file. Explains the project, how to build and run it, and outlines the features and test coverage.

	
----------------------------------------------


## Expected Output -


1. Test1.pas:


	- The interpreter initializes variable i as an INTEGER.
	- It enters a FOR-DO loop from 1 to 5.
	- On each iteration, writeln(i) is executed and the value of i is printed.
	- The final output includes the numbers 1 through 5 printed line-by-line.
# output -
		1
		2
		3
		4
		5

	


2. Test2.pas:

	- Variable x is initialized and assigned the value 1.
	- A WHILE-DO loop begins with the condition x < 5.
	- On each iteration:
			- The current value of x is printed using writeln(x).
			- x is incremented by 1.

	- The loop terminates once x = 5.
# output -

		1
		2
		3
		4




3. Test3.pas:

	- Variable i is declared and initialized in a FOR-DO loop from 1 to 10.
	
		On each iteration:

			- If i = 3, the CONTINUE statement skips the writeln.
			- If i = 7, the BREAK statement exits the loop.
			- Otherwise, writeln(i) is executed.
# output -
		1
		2
		4
		5
		6


4. Test4.pas:

	- Variables a, b, and result are declared as INTEGER.
	- Procedure greet is declared and stored.
	- Function add(x, y: INTEGER) is declared and stored.
	- In the main block:
			- a := 10 and b := 20 assignments are executed.
			- greet is called and prints:
				
					Hello from the procedure!
	
			- add(a, b) is called, computes the sum (30), and returns it.
			The result is assigned to result and printed:
# Output -

			Hello from the procedure!
			Sum is: 30



5. Test5.pas:

	- Variables x and y are declared as INTEGER.
	- In the main block:
		- x is assigned the value 3. 
		- y is assigned the value 99.
		- An IF-THEN-ELSE condition checks whether x < 5:
			- Since 3 < 5 is true, the THEN branch is executed and writeln(x) prints 3.
			- The ELSE branch is skipped.

# Output-

		3

6. Test6.pas:

	- Variable X is declared as INTEGER.

	- In the main block:
		- X is assigned the result of the expression 10 + 20, which is 30.
		- WRITELN(X) is called, printing the value of X.

# Output-

		30


	----------------------------------------------------

	## Notes-

	- This project builds on Project 2 by generating LLVM IR instead of directly interpreting Pascal/Delphi code.
	- Each feature (loops, functions, procedures, etc.) is translated into corresponding LLVM instructions.
	- Static scoping is preserved using nested Scope objects during code generation.
	- LLVM IR is split into global definitions, function bodies, and main program logic for clarity.
	- Debug messages are printed during execution to trace declarations, assignments, and function calls.