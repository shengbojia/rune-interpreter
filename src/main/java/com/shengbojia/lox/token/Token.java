package com.shengbojia.lox.token;

/**
 * Very basic implementation of tokens outputted by the scanner. Contains information about the lexeme type,
 * the lexeme itself, possible literal value, and the line number where the token is found.
 */
public class Token {
    public final TokenType type;
    public final String lexeme;
    public final Object literal;
    public final int line;

    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    @Override
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}


