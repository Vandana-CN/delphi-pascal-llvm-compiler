# delphi-pascal-llvm-compiler
Compiler for a Pascal/Delphi subset using ANTLR4 → LLVM IR; includes tests and a static demo site.


# Pascal/Delphi → LLVM Compiler (Java, ANTLR4, LLVM IR)

**Live demo:** https://vandana-cn.github.io/delphi-pascal-llvm-compiler/  
**Code:** `compiler/` (Java + ANTLR4) · **Site:** `site/` (React/Vite) · **Static build:** `docs/` (GitHub Pages)

A small compiler for a Pascal/Delphi subset. The front-end uses ANTLR4 (Java) with a visitor to emit **LLVM IR**.  
Programs run via `lli` (or Clang). The demo site shows sample programs with **downloadable `.ll`** files.

---

## Features implemented
- OO basics: **classes, constructors, encapsulation**
- Control flow: **while**, **for**, **break**, **continue**
- **Procedures & functions** (return via function name)
- **Static (lexical) scoping** across blocks/loops/functions
- I/O: integer `writeln(...)`

## Repository structure
compiler/ # Java/ANTLR4 sources, grammar, tests
site/ # React demo source (Vite)
docs/ # built static site served by GitHub Pages



## Prerequisites
- **Java JDK** 11+  
- **ANTLR 4.9.3** (JAR included in `compiler/`)  
- **LLVM** tools (for `lli`)  
- **Node.js** 20.19+ or 22.12+ (for the site)

---

## Build & run (compiler)

```bash
# from compiler/
java -jar antlr-4.9.3-complete.jar -Dlanguage=Java -visitor -package antlr -o src/antlr delphi.g4

# compile sources (Windows PowerShell – note the classpath separator ;)
javac -cp antlr-4.9.3-complete.jar -d bin src/antlr/*.java src/**/*.java

# generate IR for a test
java -cp "bin;antlr-4.9.3-complete.jar" antlr.Main tests/test1.pas

# run the LLVM IR
lli tests/test1.ll



Website (demo)
Local dev:
cd site
npm install
npm run dev

Build (outputs to ../docs):
npm run build
