package com.shengbojia.rune.callables;

import com.shengbojia.rune.Environment;
import com.shengbojia.rune.Interpreter;
import com.shengbojia.rune.ast.Stmt;
import com.shengbojia.rune.throwables.Return;

import java.util.List;

public class RuneFunction implements RuneCallable {
    private final Stmt.Function declaration;
    private final Environment closure;
    private final boolean isInit;

    /**
     * Constructor for "init" method in a class.
     *
     * @param declaration statement
     * @param closure the enclosing environment
     * @param isInit whether or the method is an initializer
     */
    public RuneFunction(Stmt.Function declaration, Environment closure, boolean isInit) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInit = isInit;
    }

    /**
     * Constructor for functions.
     *
     * @param declaration statement
     * @param closure the enclosing environment
     */
    public RuneFunction(Stmt.Function declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInit = false;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);

        // Make local copies of method arguments
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        // Catch possible return value and returns it
        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            // If 'return;' in init method, return instance ref 'this'
            if (isInit) {
                return closure.getAt(0, "this");
            }
            return returnValue.value;
        }

        // If no explicit return value in fun call, return nil
        return null;
    }

    RuneFunction bind(RuneInstance instance) {
        Environment environment = new Environment(closure);
        // "this" reference to the instance the method is called from
        environment.define("this", instance);
        return new RuneFunction(declaration, environment, isInit);
    }


    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}
