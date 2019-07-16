package com.shengbojia.lox;

import com.shengbojia.lox.Expr;
import com.shengbojia.lox.token.Token;
import com.shengbojia.lox.token.TokenType;

import java.util.List;
import java.util.function.Function;

import static com.shengbojia.lox.token.TokenType.*;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        return leftAssociativeBinary(Parser::comparison, BANG_EQUAL, EQUAL_EQUAL);
    }

    private Expr comparison() {
        return leftAssociativeBinary(Parser::addition, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL);
    }

    private Expr addition() {
        return leftAssociativeBinary(Parser::multiplication, MINUS, PLUS);
    }

    private Expr multiplication() {
        return leftAssociativeBinary(Parser::unary, STAR, SLASH);
    }

    /**
     * Higher order function that parses a left-associative series of binary operators given a list of token types and
     * an operand method reference.
     *
     * @param function     the operand method
     * @param checkedTypes the token types related to the operand method
     * @return the expression
     */
    private Expr leftAssociativeBinary(Function<Parser, Expr> function, TokenType... checkedTypes) {
        Expr expr = function.apply(this);

        while (match(checkedTypes)) {
            Token operator = previous();
            Expr right = function.apply(this);
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * Determines based on the current token whether or not there is a unary expression (!, or -). Then, recursively
     * parses until the end of the unary expression.
     *
     * @return the parsed expression
     */
    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if (match(FALSE)) {
            return new Expr.Literal(false);
        } else if (match(TRUE)) {
            return new Expr.Literal(true);
        } else if (match(NIL)) {
            return new Expr.Literal(null);
        } else if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        } else if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        } else {
            throw error(peek(), "Expect expression.");
        }
    }

    /**
     * Checks if the current, unconsumed token is of the inputted type. If so, consumes it, otherwise throws an error.
     *
     * @param type the expected token type
     * @param msg  error message to report if wrong type of token found
     * @return the consumed token if it is of the correct type
     */
    private Token consume(TokenType type, String msg) {
        if (check(type)) {
            return advance();
        } else {
            throw error(peek(), msg);
        }
    }

    /**
     * Sends an error to Lox to be reported.
     *
     * @param token which token the error occurred at
     * @param msg   error message
     * @return a runtime parse error
     */
    private ParseError error(Token token, String msg) {
        Lox.error(token, msg);
        return new ParseError();
    }

    /**
     * Advances through the tokens and discards them until a new statement is reached. By discarding the within the
     * statement containing the error, prevents cascading errors from occurring.
     */
    private void synchronize() {
        advance();

        while (!reachedEnd()) {

            // In most cases the semi-colon is the end of a statement (except for loop deceleration)
            if (previous().type == SEMICOLON) {
                return;
            }

            // These usually indicate the start of a new statement
            switch (peek().type) {
                case IF:
                case FOR:
                case FUN:
                case VAR:
                case WHILE:
                case CLASS:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

    /**
     * Checks if the current token is of any of the inputted types. If so, consumes the token and returns true.
     * Otherwise returns false and keeps the current index.
     *
     * @param types the types to check the current token against
     * @return true if the current token is of one of the inputted types, false otherwise
     */
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the current token is of the inputted type. Does not consume the current token.
     *
     * @param type the token type to check
     * @return true if the current token if of the type, false otherwise
     */
    private boolean check(TokenType type) {
        if (reachedEnd()) {
            return false;
        }

        return peek().type == type;
    }

    /**
     * Returns and then consumes the current token (advances the current index).
     *
     * @return the current token
     */
    private Token advance() {
        if (!reachedEnd()) {
            current++;
        }

        return previous();
    }

    /**
     * Checks if the current token is an EOF without consuming it.
     *
     * @return true if the current token is EOF, false otherwise
     */
    private boolean reachedEnd() {
        return peek().type == EOF;
    }

    /**
     * Returns the current token without consuming it.
     *
     * @return the current token
     */
    private Token peek() {
        return tokens.get(current);
    }

    /**
     * Returns the token prior to the current one.
     *
     * @return the previous token in tokens
     */
    private Token previous() {
        return tokens.get(current - 1);
    }

    private static class ParseError extends RuntimeException {
    }
}
