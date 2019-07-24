package com.shengbojia.lox.callables;

import com.shengbojia.lox.Environment;
import com.shengbojia.lox.Interpreter;
import com.shengbojia.lox.ast.Expr;
import com.shengbojia.lox.throwables.Return;

import java.util.List;

public class LoxLambda implements LoxCallable {
    private final Expr.Lambda lambdaExpr;

    public LoxLambda(Expr.Lambda lambdaExpr) {
        this.lambdaExpr = lambdaExpr;
    }

    @Override
    public int arity() {
        return lambdaExpr.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(interpreter.globals);

        // Make local copies of method arguments
        for (int i = 0; i < lambdaExpr.params.size(); i++) {
            environment.define(lambdaExpr.params.get(i).lexeme, arguments.get(i));
        }

        // Catch possible return value and returns it
        try {
            interpreter.executeBlock(lambdaExpr.body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }

        // If no explicit return value in fun call, return nil
        return null;

    }
}
