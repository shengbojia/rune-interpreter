package com.shengbojia.lox.token;

/**
 * Enum class of different lexeme types in Lox.
 */
public enum TokenType {

    // Single character punctuation tokens. eg ( ) { } , .
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, SEMICOLON,

    // Ternary
    QUERY, COLON,

    // Math operators
    MINUS, PLUS, SLASH, STAR,

    // Comparators
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Literals
    IDENTIFIER, STRING, NUMBER,

    // Keywords
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
    PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

    // End of file
    EOF

}
