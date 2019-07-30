package com.shengbojia.rune;

import com.shengbojia.rune.ast.Expr;
import com.shengbojia.rune.ast.Stmt;
import com.shengbojia.rune.token.Token;

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
    private ClassType currentClass = ClassType.NONE;

    /**
     * Enum for what kind of function body the resolver is currently in
     */
    private enum FunctionType {
        NONE,
        FUNCTION,
        INIT,
        METHOD,
        CLASS_METHOD
    }

    /**
     * Enum for what kind of class body the resolver is currently in
     */
    private enum ClassType {
        NONE,
        CLASS
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
            Rune.error(name, "Variable with the same name already defined in this scope.");
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
        FunctionType enclosingFuncType = currentFunction;
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
        currentFunction = enclosingFuncType;
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
        ClassType enclosingClassType = currentClass;
        currentClass = ClassType.CLASS;

        declare(stmt.name);
        define(stmt.name);


        if (stmt.superClass != null) {
            // weird edge case: class foo : foo {}
            if (stmt.name.lexeme.equals(stmt.superClass.name.lexeme)) {
                Rune.error(stmt.superClass.name, "A class cannot inherit from itself.");
            }
            resolve(stmt.superClass);
        }

        // push a new scope and define "this" as a variable
        beginScope();
        scopes.peek().put("this", true);

        for (Stmt.Function method : stmt.methods) {
            FunctionType declaration = FunctionType.METHOD;
            if (method.name.lexeme.equals("init")) {
                declaration = FunctionType.INIT;
            }
            resolveFunction(method, declaration);
        }

        for (Stmt.Function classMethod : stmt.classMethods) {
            FunctionType declaration = FunctionType.CLASS_METHOD;

            resolveFunction(classMethod, declaration);
        }

        endScope();
        currentClass = enclosingClassType;

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
            Rune.error(stmt.keyword, "Cannot return from top-level code.");
        }

        if (stmt.value != null) {
            if (currentFunction == FunctionType.INIT) {
                Rune.error(stmt.keyword, "Cannot return a value from an instance initializer.");
            }
            resolve(stmt.value);
        }

        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        if (!inALoop) {
            Rune.error(stmt.keyword, "Cannot use break when not in a loop.");
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
            Rune.error(expr.name, "Cannot read local variable in its own initializer.");
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
    public Void visitGetExpr(Expr.Get expr) {
        resolve(expr.object);
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
    public Void visitSetExpr(Expr.Set expr) {
        resolve(expr.value);
        resolve(expr.object);

        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This expr) {
        if (currentClass == ClassType.NONE) {
            Rune.error(expr.keyword, "Cannot use 'this' outside a class.");
            return null;
        }
        resolveLocal(expr, expr.keyword);
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
