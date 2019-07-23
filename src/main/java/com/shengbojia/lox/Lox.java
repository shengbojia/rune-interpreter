package com.shengbojia.lox;

import com.shengbojia.lox.ast.Stmt;
import com.shengbojia.lox.errors.RuntimeError;
import com.shengbojia.lox.token.Token;
import com.shengbojia.lox.token.TokenType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    // Static instance so when in REPL, can keep track of global variables
    private static Interpreter interpreter = new Interpreter();

    private static boolean hadError = false; // Ensure we don't execute code with an error
    private static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {

        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(42);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt(); // If you run 'jlox' without arguments, an interactive prompt will start
        }

    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) {
            System.exit(43);
        }

        if (hadRuntimeError) {
            System.exit(44);
        }
    }

    /**
     * Starts a REPL that asks user for input.
     * @throws IOException
     */
    @SuppressWarnings("InfiniteLoopStatement")
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (; ; ) {
            System.out.print("> ");
            run(reader.readLine());

        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // For now, just print the expressions.
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        // Stop if a syntax error occurs
        if (hadError) {
            return;
        }

        interpreter.interpret(statements);

        // Below is for testing the parser
        // System.out.println(new AstPrinter().print(expression));
    }

    /**
     * Reports an error with the given message at the given line.
     *
     * @param line the line the error occurred at
     * @param msg the error message
     */
    static void error(int line, String msg) {
        report(line, "", msg);
    }

    /**
     * Reports an error which occurred at the given token, attached with the inputted error message.
     *
     * @param token the token the error occurred at
     * @param msg the error message
     */
    static void error(Token token, String msg) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", msg);

        } else {
            report(token.line, " at " + token.lexeme + "'", msg);
        }

    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
                "\n[line " + error.token.line + "]");

        hadRuntimeError = true;
    }

    /**
     * Displays an error based on the given inputs.
     *
     * @param line where error occurred
     * @param where where within the line the error occurred
     * @param msg error message to display
     */
    private static void report(int line, String where, String msg) {
        System.err.println(
                "[line " + line + "] Error " + where +": " + msg
        );

        hadError = true;
    }

}

/*

class AstPrinter implements Expr.Visitor<String> {

    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitTernaryExpr(Expr.Ternary expr) {
        return parenthesize(expr.firstOp.lexeme + expr.secondOp.lexeme, expr.left, expr.middle, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    /**
     * Formats the inputted expressions into a parenthesized, Polish notation string.
     *
     * @param name the associated tag/name of the expressions
     * @param exprs the expressions to format
     * @return a Polish notation formatted expression
     */

/*
    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }
}

*/


