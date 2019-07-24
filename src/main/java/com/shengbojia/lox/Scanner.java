package com.shengbojia.lox;

import com.shengbojia.lox.token.Token;
import com.shengbojia.lox.token.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
        keywords.put("lambda", LAMBDA);
    }

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
            case '?':
                addToken(QUERY);
                break;
            case ':':
                addToken(COLON);
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

            case '/': // The / char could be the beginning of a comment
                handleSlash();
                break;

            case ' ': // Ignore whitespace
            case '\r':
            case '\t':
                break;

            case '\n': // Ignore newline but increase line count
                line++;
                break;

            case '"': // Beginning of a string
                handleString();
                break;

            default:
                if (isDigit(c)) {
                    handleNumber();
                } else if (isAlpha(c)) {
                    handleIdentifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
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
        addToken(type, null);
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
     *
     * @return the character seen or '\0' if EOF
     */
    private char peek() {
        if (reachedEnd()) {
            return '\0';
        }

        return source.charAt(current);
    }

    /**
     * Look ahead by inputted number of characters.
     *
     * @param distance how many characters to look ahead
     * @return char seen or '\0' if looking at/beyond EOF
     */
    private char peekFurther(int distance) {
        if (current + distance >= source.length()) {
            return '\0';
        }

        return source.charAt(current + distance);
    }

    /**
     * Checks if the '/' is the start of a line comment, block comment, or neither. Ignores and advances through
     * commented out text.
     * <p>
     * Lox follows C-style commenting, so supports // and /* .. * / comments.
     */
    private void handleSlash() {
        // Single line comment
        if (secondCharIs('/')) {
            // Lox is C-style comments, so goes until end of line
            while ((peek() != '\n') && !reachedEnd()) {
                advance(); // Keep advancing until newline or EOF, thus ignoring the comment lexeme
            }
        } else if (secondCharIs('*')) {
            // Block comments go until * / is reached
            while (!reachedEnd()) {
                if (peek() == '*' && peekFurther(1) == '/') {
                    advance();
                    advance();
                    return;
                }
                advance();
            }
        } else {
            addToken(SLASH);
        }
    }

    /**
     * Consumes characters until a ", or EOF is reached. If a " is reached successfully, adds a string literal token
     * composed of the consumed characters to the list of tokens. Otherwise reports an error.
     */
    private void handleString() {
        while (peek() != '"' && !reachedEnd()) {
            if (peek() == '\n') {
                line++; // Lox supports multiline strings, unlike java
            }
            advance();
        }

        // Unterminated String
        if (reachedEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // advance to the closing "
        advance();

        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    /**
     * Checks if the inputted char is a numerical digit.
     *
     * @param toCheck the char to check
     * @return true if input is a digit, false otherwise
     */
    private boolean isDigit(char toCheck) {
        return toCheck >= '0' && toCheck <= '9';
    }

    /**
     * Checks if the inputted char is an alphabetical letter or an underscore '_'.
     *
     * @param toCheck the char to check
     * @return true if input is a letter or _, false otherwise
     */
    private boolean isAlpha(char toCheck) {
        return (toCheck >= 'a' && toCheck <= 'z') ||
                (toCheck >= 'A' && toCheck <= 'Z') ||
                toCheck == '_';
    }

    /**
     * Checks if the inputted char is alpha-numerical.
     *
     * @param toCheck the char to check
     * @return true if input is alphanumeric, false otherwise
     */
    private boolean isAlphaNumeric(char toCheck) {
        return isAlpha(toCheck) || isDigit(toCheck);
    }

    /**
     * Consumes as many numerical digits as possible. If a decimal is reached, checks whether or not more digits follow
     * the decimal, since Lox allows for number method calls. (eg 140.abs())
     * <p>
     * Lox has only floating point numbers. Allows for integer and decimal literals, but no leading/trailing decimals.
     * eg. 555, 55.5 okay. But .555 or 555. are bad.
     * <p>
     * Also, negative numbers are considered an expression, so -100 is not a literal.
     */
    private void handleNumber() {
        while (isDigit(peek())) {
            advance();
        }

        // Look for decimals; Will be allowing extension calls like 140.abs() so make sure it's a numerical decimal
        if (peek() == '.' && isDigit(peekFurther(1))) {

            // Consume the decimal
            advance();

            // Consume the digits following the decimal
            while (isDigit(peek())) {
                advance();
            }
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    /**
     * Consumes characters (alphanumeric ones) part of an identifier lexeme. Then checks if the lexeme is a keyword of
     * Lox before adding the appropriate type of token to the list of tokens.
     * <p>
     * Lox allows for identifiers to start with an underscore. So _myVar is fine.
     */
    private void handleIdentifier() {

        while (isAlphaNumeric(peek())) {
            advance();
        }

        // Get the lexeme
        String text = source.substring(start, current);

        // Find a matching keyword (if any)
        TokenType type = keywords.get(text);
        if (type == null) {
            type = IDENTIFIER; // ie not a keyword
        }
        addToken(type);
    }

}
