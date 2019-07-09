package com.shengbojia.lox;

import com.shengbojia.lox.token.Token;
import com.shengbojia.lox.token.TokenType;

import java.util.ArrayList;
import java.util.List;

import static com.shengbojia.lox.token.TokenType.*;

/**
 * Scanner for the interpreter.
 */
public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;

    Scanner(String source) {
        this.source = source;
    }

    /**
     * Loops through each character in the source, grouping the characters by lexeme and then sorts them into tokens.
     *
     * @return list of scanned tokens
     */
    List<Token> scanTokens() {

        while (!reachedEnd()) {

            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    /**
     * Checks whether or not the scanner has reached the end of the file.
     *
     * @return true if end of file reached
     */
    private boolean reachedEnd() {
        return current >= source.length();
    }

    /**
     * Scans for a single token.
     */
    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case ',':
                addToken(COMMA);
                break;
            case '.':
                addToken(DOT);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '+':
                addToken(PLUS);
                break;
            case ';':
                addToken(SEMICOLON);
                break;
            case '*':
                addToken(STAR);
                break;

            case '!': // These characters could be part of a 2-char lexeme, so must check the second char.
                addToken(secondCharIs('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(secondCharIs('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(secondCharIs('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(secondCharIs('=') ? GREATER_EQUAL : GREATER);
                break;

            case '/': // The / char could be the beginning of a comment, so check that.
                if (secondCharIs('/')) {
                    // Lox is C-style comments, so goes until end of line
                    while ((peek() != '\n') && !reachedEnd()) {
                        advance(); // Keep advancing until newline or EOF, thus ignoring the comment lexeme
                    }
                } else {
                    addToken(SLASH);
                }
                break;

            case ' ': // Ignore whitespace
            case '\r':
            case '\t':
                break;

            case '\n': // Ignore newline but increase line count
                line++;
                break;

            default:
                Lox.error(line, "Unexpected character.");
                break;
        }
    }

    /**
     * Returns the current character and advances the current index by 1.
     *
     * @return the current character
     */
    private char advance() {
        current++;
        return source.charAt(current - 1);
    }

    /**
     * Adds a non-literal token of the specified type to the token list.
     *
     * @param type the type of token to add to tokens
     */
    private void addToken(TokenType type) {

    }

    /**
     * Adds a literal token of the specified type with the specified value to the token list.
     *
     * @param type    the type of token
     * @param literal the literal value of the token
     */
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    /**
     * Checks if the input character matches the current character, and if so advance the current index by 1.
     *
     * @param expected the input character to match
     * @return true if current character matches, false otherwise
     */
    private boolean secondCharIs(char expected) {
        if (reachedEnd()) {
            return false;
        }

        if (source.charAt(current) != expected) {
            return false;
        }

        current++;
        return true;
    }

    /**
     * Look ahead by one character.
     * @return the character seen or '\0' if EOF
     */
    private char peek() {
        if (reachedEnd()) {
            return '\0';
        }

        return source.charAt(current);
    }


}
