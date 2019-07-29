package com.shengbojia.lox;

import com.shengbojia.lox.ast.Expr;
import com.shengbojia.lox.ast.Stmt;
import com.shengbojia.lox.token.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * A resolver that passes through the syntax tree and calculates how many steps down the environment chain each
 * variable usage is.
 */
public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;
    private boolean inALoop = false;

    /**
     * Enum for what kind of function the resolver is currently in
     */
    private enum FunctionType {
        NONE,
        FUNCTION
    }

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    void resolve(List<Stmt> statements) {
        for (Stmt stmt : statements) {
            resolve(stmt);
        }
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    private void endScope() {
        scopes.pop();
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) {
            return;
        }

        Map<String, Boolean> scope = scopes.peek();
        // I will allow multiple declaration of same name in global, but not locally
        if (scope.containsKey(name.lexeme)) {
            Lox.error(name, "Variable with the same name already defined in this scope.");
        }

        scope.put(name.lexeme, false);
    }

    private void define(Token name) {
        if (scopes.isEmpty()) {
            return;
        }

        scopes.peek().put(name.lexeme, true);
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }

        // not found locally
    }

    private void resolveFunction(Stmt.Function function, FunctionType type) {
        // keep track of enclosing
        FunctionType enclosingType = currentFunction;
        currentFunction = type;

        beginScope();
        // resolve function params inside scope
        for (Token param : function.params) {
            declare(param);
            define(param);
        }

        // resolve any local var in function body
        resolve(function.body);
        endScope();

        // pop back to enclosing
        currentFunction = enclosingType;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();

        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        declare(stmt.name);
        define(stmt.name);

        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        // resolve function ref in surrounding scope
        declare(stmt.name);
        define(stmt.name);

        // step into the function's scope and resolve param/var inside
        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) {
            resolve(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        // Make sure the return stmt is actually in a function
        if (currentFunction == FunctionType.NONE) {
            Lox.error(stmt.keyword, "Cannot return from top-level code.");
        }

        if (stmt.value != null) {
            resolve(stmt.value);
        }

        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        if (!inALoop) {
            Lox.error(stmt.keyword, "Cannot use break when not in a loop.");
        }

        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.name);
        // If there is an initializer, check if the variable mentions itself in it
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        boolean enclosedInLoop = inALoop;
        inALoop = true;

        resolve(stmt.condition);
        resolve(stmt.body);

        inALoop = enclosedInLoop;
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        // Only time this condition holds true is while resolving the variable's initializer
        if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
            // Compile error
            Lox.error(expr.name, "Cannot read local variable in its own initializer.");
        }

        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitTernaryExpr(Expr.Ternary expr) {
        resolve(expr.left);
        resolve(expr.middle);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);

        for (Expr arg : expr.arguments) {
            resolve(arg);
        }

        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitLambdaExpr(Expr.Lambda expr) {
        beginScope();
        for (Token param : expr.params) {
            declare(param);
            define(param);
        }
        resolve(expr.body);
        endScope();
        return null;
    }
}
