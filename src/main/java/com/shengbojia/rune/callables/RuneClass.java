package com.shengbojia.rune.callables;

import com.shengbojia.rune.Interpreter;

import java.util.List;
import java.util.Map;

public class RuneClass implements RuneCallable {
    public final String name;
    private final Map<String, RuneFunction> methods;

    public RuneClass(String name, Map<String, RuneFunction> methods) {
        this.name = name;
        this.methods = methods;
    }

    RuneFunction findMethod(String name) {
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
    public String toString() {
        return name + "::class";
    }
}
