package com.shengbojia.lox;

import com.shengbojia.lox.ast.Expr;
import com.shengbojia.lox.ast.Stmt;
import com.shengbojia.lox.throwables.ParseError;
import com.shengbojia.lox.token.Token;
import com.shengbojia.lox.token.TokenType;

import java.util.ArrayList;
import java.util.Arrays;
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
     * declaration  -> funDeclaration
     *               | varDeclaration
     *               | statement ;
     */
    private Stmt declaration() {
        try {
            if (match(FUN)) {
                return function("function");
            } else if (match(VAR)) {
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
     * funDeclaration  -> "fun" function ;
     * function        -> IDENTIFIER "(" parameters? ")" block ;
     * parameters      -> IDENTIFIER ( "," IDENTIFIER )* ;
     *
     * @param kind could be function or method
     */
    private Stmt.Function function(String kind) {
        Token name = consume(IDENTIFIER, "Expect " + kind + " name.");

        consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
        List<Token> params = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (params.size() >= 32) {
                    error(peek(), "Cannot have more than 32 parameters.");
                }
                params.add(consume(IDENTIFIER, "Expect parameter name."));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");

        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> body = block();

        return new Stmt.Function(name, params, body);
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
     *             | forStatement
     *             | ifStatement
     *             | printStatement
     *             | returnStatement
     *             | breakStatement
     *             | whileStatement
     *             | block ;
     */
    private Stmt statement() {
        if (match(FOR)) {
            return forStatement();
        } else if (match(IF)) {
            return ifStatement();
        } else if (match(PRINT)) {
            return printStatement();
        } else if (match(RETURN)) {
            return returnStatement();
        } else if (match(BREAK)) {
            return breakStatement();
        } else if (match(WHILE)) {
            return whileStatement();
        } else if (match(LEFT_BRACE)) {
            return new Stmt.Block(block());
        }

        return expressionStatement();
    }

    /**
     * Instead of having a dedicated for loop syntax tree node, we will desugar it.
     * eg) for(var i = 0; i < 2; i++) {} becomes:
     * var i = 0; while(i < 2) {i++;}
     * <p>
     * forStatement  -> "for" "(" (varDeclaration | expressionStatement | ";")
     *                            expression? ";"
     *                            expression? ")" statement ;
     */
    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");

        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after for loop condition");

        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.");

        Stmt body = statement();

        // Place the increment stmt after the body in a new block
        if (increment != null) {
            body = new Stmt.Block(Arrays.asList(
                    body,
                    new Stmt.Expression(increment)
            ));
        }

        // for(... ; ; ...) becomes while(true)
        if (condition == null) {
            condition = new Expr.Literal(true);
        }

        // Essentially a while loop now
        body = new Stmt.While(condition, body);

        // Place initializer before body in a new block
        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(
                    initializer,
                    body
            ));
        }

        return body;
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
     * returnStatement  -> "return" expression? ";" ;
     */
    private Stmt returnStatement() {
        Token keyword = previous();

        // Default return is nil (null)
        Expr value = null;

        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    /**
     * breakStatement  -> "break" ";" ;
     */
    private Stmt breakStatement() {
        Token keyword = previous();

        consume(SEMICOLON, "Expect ';' after 'break' keyword.");
        return new Stmt.Break(keyword);
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
        Expr expr = lambda();

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
     * lambda      -> "lambda" "(" parameters? ")" block ;
     * parameters  -> IDENTIFIER ( "," IDENTIFIER )* ;
     *
     */
    private Expr lambda() {
        if (match(LAMBDA)) {

            consume(LEFT_PAREN, "Expect '(' after 'lambda'.");
            List<Token> params = new ArrayList<>();
            if (!check(RIGHT_PAREN)) {
                do {
                    if (params.size() >= 32) {
                        error(peek(), "Cannot have more than 32 parameters in lambda.");
                    }
                    params.add(consume(IDENTIFIER, "Expect parameter name."));
                } while (match(COMMA));
            }

            consume(RIGHT_PAREN, "Expect ')' after lambda parameters.");

            consume(LEFT_BRACE, "Expect '{' before lambda body.");
            List<Stmt> body = block();

            return new Expr.Lambda(params, body);
        }


        return logicalOr();
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
     * unary  -> ( "!" | "-" ) unary | call ;
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

        return call();
    }

    /**
     * call  -> primary ( "(" arguments? ")" )* ;
     */
    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else {
                break;
            }
        }

        return expr;
    }

    /**
     * Helper method to parse argument list.
     * <p>
     * arguments  -> expression ( "," expression )* ;
     *
     * @param callee the callee
     */
    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();

        if (!check(RIGHT_PAREN)) {
            do {
                // java has a max argument size of 255, let's have 32 on Lox
                if (arguments.size() >= 32) {
                    error(peek(), "Cannot have more than 32 arguments.");
                }
                arguments.add(expression());
            } while (match(COMMA));
        }

        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");

        return new Expr.Call(callee, paren, arguments);
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
     * statement containing the error, prevents cascading throwables from occurring.
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
