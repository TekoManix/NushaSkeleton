# NushaSkeleton

A Java implementation of the **Nusha** language — a rule-based DSL with lexing, parsing, and interpretation. Built Fall 2025.

## Structure

```
src/
├── Lexer.java                # Tokenizes input text (indentation-aware)
├── NushaFall2025Parser.java  # Builds AST from token stream
├── Interpreter.java          # Executes the AST
├── TextManager.java          # Character-by-character input handling
├── TokenManager.java         # Token stream operations
├── SyntaxErrorException.java # Parse error signaling
└── AST/                      # AST node types
    ├── Nusha.java            # Root node (definitions, variables, rules)
    ├── Definition.java       # Named definition with choices
    ├── Variable.java         # Variable bound to a definition
    ├── Rule.java             # Condition + then-clauses
    ├── Expression.java       # Binary expression (left op right)
    ├── Op.java               # Operator enum
    └── ...                   # Supporting node types
```

## Language Features

- **Definitions** — named sets of choices (similar to enums)
- **Variables** — instances mapped to a definition
- **Structures** — custom types with named member variables
- **Rules** — if-then constraints over variables
- **Uniqueness constraints** — enforce variable values are distinct

## Build

Compile all sources in `src/` with Java 11+:

```bash
javac -d out src/**/*.java src/*.java
```

## Test

Tests use JUnit 5.8.1. Run from your IDE or via JUnit launcher:

```
src/Lexer1Test.java       # Basic lexer tests
src/Lexer2Tests.java      # Comprehensive lexer tests
src/Parser1Tests.java     # Basic parser tests
src/Parser2Tests.java     # Comprehensive parser tests
src/InterpreterTests.java # End-to-end interpreter tests
```

## Pipeline

```
Input text
  → Lexer (Token stream)
    → NushaFall2025Parser (AST)
      → Interpreter (execution result)
```
