package com.shengbojia.rune.callables;

import com.shengbojia.rune.Interpreter;
import com.shengbojia.rune.throwables.RuntimeError;
import com.shengbojia.rune.token.Token;

import java.util.List;
import java.util.Map;

public class RuneClass extends RuneInstance implements RuneCallable, RuneClassDesc {
    public final String name;
    private final RuneClass superClass;
    private final Map<String, RuneFunction> methods;


    public RuneClass(RuneMetaClass metaClass, String name, RuneClass superClass, Map<String, RuneFunction> methods) {
        super(metaClass);
        this.name = name;
        this.superClass = superClass;
        this.methods = methods;
    }

    @Override
    public RuneFunction getProperty(Token name) {
        RuneFunction classMethod = super.runeClassDesc.findMethod(name.lexeme);
        if (classMethod != null) {
            return classMethod;
        }

        throw new RuntimeError(name, "No such static method found: " + name.lexeme + ".");
    }

    @Override
    public RuneFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        return null;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        RuneInstance instance = new RuneInstance(this);

        // if user-define constructor is found, bind a 'this' instance ref and call the constructor
        RuneFunction initializer = findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }

    @Override
    public int arity() {
        RuneFunction initializer = findMethod("init");
        // ie default constructor
        if (initializer == null) {
            return 0;
        }
        return initializer.arity();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + "::class";
    }
}
