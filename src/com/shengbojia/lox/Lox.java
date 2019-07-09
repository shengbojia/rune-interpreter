package com.shengbojia.lox;

import com.shengbojia.lox.token.Token;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    static boolean hadError = false; // Ensure we don't execute code with an error

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

        // For now, just print the tokens.
        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    static void error(int line, String msg) {
        report(line, "", msg);
    }

    /**
     *
     * @param line where error occurred
     * @param where
     * @param msg
     */
    private static void report(int line, String where, String msg) {
        System.err.println(
                "[line " + line + "] Error " + where +": " + msg
        );

        hadError = true;
    }

}
