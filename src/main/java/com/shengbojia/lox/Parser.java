package com.shengbojia.lox;

import com.shengbojia.lox.errors.ParseError;
import com.shengbojia.lox.token.Token;
import com.shengbojia.lox.token.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.shengbojia.lox.token.TokenType.*;

/**
 * Parser class that uses the following Context-Free Grammar:
 * <p>
 * ->   indicates production
 * |    indicates a choice between a series of productions
 * ()   is used for grouping
 * *    a postfix for recursion, means preceding symbol/group maybe be repeated 0 or more times
 * +    a postfix for recursion, similar to above but means at least 1 time.
 * ?    postfix meaning preceding symbol/group appears 0 or 1 time.
 * ;    not to be confused with the Lox statement end ';', this is a metalanguage symbol indicating end of current rule
 */
public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * program  -> declaration* EOF ;
     */
    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!reachedEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    /**
     * declaration  -> varDeclaration
     *               | statement ;
     */
    private Stmt declaration() {
        try {
            if (match(VAR)) {
                return varDeclaration();
            }
            return statement();
        } catch (ParseError error) {

            // Synchronize at this level for error recovery
            synchronize();
            return null;
        }
    }

    /**
     * varDeclaration  -> "var" IDENTIFIER ( "=" expression )? ";" ;
     */
    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;

        // Variable could be initialized
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);

    }

    /**
     * statement  -> expressionStatement
     *             | ifStatement
     *             | printStatement
     *             | whileStatement
     *             | block ;
     */
    private Stmt statement() {
        if (match(IF)) {
            return ifStatement();
        } else if (match(PRINT)) {
            return printStatement();
        } else if (match(WHILE)) {
            return whileStatement();
        } else if (match(LEFT_BRACE)) {
            return new Stmt.Block(block());
        }

        return expressionStatement();
    }

    /**
     * ifStatement  -> "if" "(" expression ")" statement ( "else" statement )? ;
     */
    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect '(' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;

        // There may or may not be an else branch
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    /**
     * printStatement  -> "print" expression ";" ;
     */
    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';'after value.");
        return new Stmt.Print(value);
    }

    /**
     * expressionStatement  -> expression ";" ;
     */
    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression");
        return new Stmt.Expression(expr);
    }

    /**
     * whileStatement  -> "while" "(" expression ")" statement ;
     */
    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after while condition");

        Stmt body = statement();

        return new Stmt.While(condition, body);
    }

    /**
     * block  -> "{" declaration* "}" ;
     */
    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        // Keep parsing and adding to the list until '}' is reached or EOF occurs
        while (!check(RIGHT_BRACE) && !reachedEnd()) {
            statements.add(declaration());
        }

        // Make sure the block has a closing brace
        consume(RIGHT_BRACE, "Expect '}' at the end of block.");
        return statements;
    }

    /**
     * expression  -> comma ;
     */
    private Expr expression() {
        return comma();
    }

    /**
     * C-style comma operator. Lowest precedence of all operators.
     *
     * comma  -> assignment ","
     */
    private Expr comma() {
        return leftAssociativeBinary(Parser::assignment, COMMA);
    }

    /**
     * assignment  -> IDENTIFIER "=" assignment
     *              | conditional ;
     */
    private Expr assignment() {
        Expr expr = conditional();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    /**
     * C-style ternary conditional operator '?:'. Third lowest precedence, just above the assignment operator.
     * <p>
     * It is right associative thus implemented via right recursion, the middle operand is treated as parenthesized and
     * thus can be any expression.
     * <p>
     * conditional  -> logicalOr "?" expression ":" conditional ;
     */
    private Expr conditional() {
        Expr expr = logicalOr();

        if (match(QUERY)) {
            Token query = previous();
            Expr middle = expression(); // In C, the middle operand of ?: is treated as parenthesized
            Token colon = consume(COLON, "Expect ':' after expression.");
            Expr right = conditional(); // Right associative
            expr = new Expr.Ternary(expr, query, middle, colon, right);
        }

        return expr;
    }

    /**
     * Short circuit logical or.
     * <p>
     * logicalOr  -> logicalAnd ( "or" logicalAnd )* ;
     */
    private Expr logicalOr() {
        return leftAssociativeBinary(Parser::logicalAnd, OR);
    }

    /**
     * Short circuit logical and.
     * <p>
     * logicalAnd  -> equality ( "and" equality )* ;
     */
    private Expr logicalAnd() {
        return leftAssociativeBinary(Parser::equality, AND);
    }

    /**
     * equality  -> comparison ( ( "!=" | "==" ) comparison )* ;
     */
    private Expr equality() {
        return leftAssociativeBinary(Parser::comparison, BANG_EQUAL, EQUAL_EQUAL);
    }

    /**
     * comparison  -> addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
     */
    private Expr comparison() {
        return leftAssociativeBinary(Parser::addition, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL);
    }

    /**
     * addition  -> multiplication ( ( "-" | "+" ) multiplication )* ;
     */
    private Expr addition() {
        return leftAssociativeBinary(Parser::multiplication, MINUS, PLUS);
    }

    /**
     * multiplication  -> unary ( ( "/" | "*" ) unary )* ;
     */
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
     * <p>
     * unary  -> ( "!" | "-" ) unary
     *         | primary ;
     */
    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        // Make sure a binary operator does not appear at the start of an expression.
        checkBinaryNoLeftError();

        return primary();
    }


    /**
     * primary  -> NUMBER
     *           | STRING
     *           | "false"
     *           | "true"
     *           | "nil"
     *           | "(" expression ")" ;
     */
    private Expr primary() {
        if (match(FALSE)) {
            return new Expr.Literal(false);
        } else if (match(TRUE)) {
            return new Expr.Literal(true);
        } else if (match(NIL)) {
            return new Expr.Literal(null);
        } else if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        } else if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
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
     * Checks if any binary operator lacks a left operand (appears at the beginning of an expression). If so, parses and
     * discards the remaining right operand, and then throws a parse error.
     */
    private void checkBinaryNoLeftError() {

        Token errorToken;

        if (match(COMMA)) {
            errorToken = previous();
            conditional();
        } else if (match(QUERY)) {
            errorToken = previous();
            expression();
        } else if (match(BANG_EQUAL, EQUAL_EQUAL)) {
            errorToken = previous();
            comparison();
        } else if (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            errorToken = previous();
            addition();
        } else if (match(PLUS, MINUS)) {
            errorToken = previous();
            multiplication();
        } else if (match(STAR, SLASH)) {
            errorToken = previous();
            unary();
        } else {
            return;
        }

        throw error(errorToken, "Expected a left operand.");
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
}
